package com.example.ryomap;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Maptest extends AppCompatActivity {

    private GoogleMap mMap;
    private Button zoomInBtn, zoomOutBtn;

    // マーカーを管理するリスト
    private List<Marker> markers = new ArrayList<>();

    // 初期位置が設定されたかどうかを管理するフラグ
    private boolean isInitialPositionSet = false;

    // ボタンのリストを作成
    private List<MaterialButton> categoryButtons = new ArrayList<>();
    private ColorStateList defaultColor; // ボタンのデフォルト背景色



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        Button backmenu_btn = findViewById(R.id.backmenu_btn); //画面遷移先のトリガーとなるボタン指定で
        backmenu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(Maptest.this, MenuActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });

        // ズームインボタンの設定
        zoomInBtn = findViewById(R.id.zoom_in_btn);
        zoomInBtn.setOnClickListener(v -> zoomIn());

        // ズームアウトボタンの設定
        zoomOutBtn = findViewById(R.id.zoom_out_btn);
        zoomOutBtn.setOnClickListener(v -> zoomOut());

        // Map Fragment の設定
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                onMapReady(googleMap); // 初期化処理をここで呼び出す
            });
        }

        // 各ボタンの設定
        MaterialButton sightseeingButton = findViewById(R.id.sightseeingbutton);
        MaterialButton foodButton = findViewById(R.id.foodbutton);
        MaterialButton hotelButton = findViewById(R.id.hotelbutton);
        MaterialButton eventButton = findViewById(R.id.eventbutton);
        MaterialButton favoriteButton = findViewById(R.id.favoritebutton);
        MaterialButton secretButton = findViewById(R.id.secretbutton);

        // ボタンリストに追加
        categoryButtons.add(sightseeingButton);
        categoryButtons.add(foodButton);
        categoryButtons.add(hotelButton);
        categoryButtons.add(eventButton);
        categoryButtons.add(favoriteButton);
        categoryButtons.add(secretButton);

        // デフォルトの背景色を透明な白 (#80FFFFFF) に設定
        defaultColor = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"));

        // 各ボタンにクリックリスナーを設定
        sightseeingButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(1);
            changeButtonColor(sightseeingButton); // 色変更
        });
        foodButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(2);
            changeButtonColor(foodButton); // 色変更
        });
        hotelButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(3);
            changeButtonColor(hotelButton); // 色変更
        });
        eventButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(4);
            changeButtonColor(eventButton); // 色変更
        });
        favoriteButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(5);
            changeButtonColor(favoriteButton); // 色変更
        });
        secretButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(6);
            changeButtonColor(secretButton); // 色変更
        });
    }

    // ボタンを押したときの色変更
    private void changeButtonColor(MaterialButton selectedButton) {
        // 他のボタンの色をリセット
        for (MaterialButton button : categoryButtons) {
            button.setBackgroundTintList(defaultColor);
        }
        // 選択されたボタンの色を変更
        selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#80FFCC00"))); // 黄色に変更
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 初期位置が設定されていない場合のみ設定
        if (!isInitialPositionSet) {
            // 初期位置の設定
            LatLng kochi = new LatLng(33.620, 133.719);  // 例: 高知工科大学
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kochi, 10));  // ズームレベル11
            isInitialPositionSet = true;  // 初期位置が設定されたことを記録
        }

        // マーカークリックリスナーの設定
        mMap.setOnMarkerClickListener(marker -> {
            // DetailActivity への遷移処理
            Intent intent = new Intent(Maptest.this, DetailActivity.class);
            intent.putExtra("markerTitle", marker.getTitle());  // タイトル情報を渡す
            intent.putExtra("markerAddress", marker.getPosition().toString());
            startActivity(intent);
            return true; // イベント消費
        });
    }

    // スポットを表示するメソッド
    private void fetchAndDisplaySpots(int kategoriId) {
        clearMarkers();  // 新しいスポットを表示する前に既存のマーカーを削除
        new FetchSpotsTask().execute(kategoriId);
    }

    private class FetchSpotsTask extends AsyncTask<Integer, Void, List<Spot>> {

        @Override
        protected List<Spot> doInBackground(Integer... params) {
            int kategoriId = params[0];
            List<Spot> spots = new ArrayList<>();

            try {
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";
                String user = "root";
                String password = "h11y24n20";

                Connection connection = DriverManager.getConnection(url, user, password);

                if (connection != null) {
                    String query = "SELECT spot_name, address, image FROM spot WHERE kategori_id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, kategoriId);

                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        String name = resultSet.getString("spot_name");
                        String address = resultSet.getString("address");
                        String imagePath = resultSet.getString("image");  // imageを取得
                        spots.add(new Spot(name, address, imagePath));
                    }

                    resultSet.close();
                    statement.close();
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return spots;
        }

        @Override
        protected void onPostExecute(List<Spot> spots) {
            super.onPostExecute(spots);

            if (spots.isEmpty()) {
                Toast.makeText(Maptest.this, "該当するスポットが見つかりませんでした", Toast.LENGTH_SHORT).show();
            } else {
                for (Spot spot : spots) {
                    geocodeAddressAndAddPin(spot.getName(), spot.getAddress());
                }
            }
        }
    }

    // サイズを変更するメソッド
    private Bitmap resizeBitmap(int resourceId, int width, int height) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    // ピンを追加するメソッド
    private void geocodeAddressAndAddPin(String name, String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);

                // デフォルトのピンを追加
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name));
                        //.icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap(R.drawable.menumap, 80, 100))));
                // 追加したマーカーを markers リストに保存
                markers.add(marker);  // リストに追加する
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "住所の変換に失敗しました: " + address, Toast.LENGTH_SHORT).show();
        }
    }

    // マーカーを削除するメソッド
    private void clearMarkers() {
        for (Marker marker : markers) {
            marker.remove();  // マーカーを削除
        }
        markers.clear();  // リストを空にする
    }

    // ズームインの処理
    private void zoomIn() {
        if (mMap != null) {
            float zoomLevel = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, zoomLevel + 1));
        }
    }

    // ズームアウトの処理
    private void zoomOut() {
        if (mMap != null) {
            float zoomLevel = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, zoomLevel - 1));
        }
    }

    public static class Spot {
        private final String name;
        private final String address;
        private final String image;

        public Spot(String name, String address, String image) {
            this.name = name;
            this.address = address;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getImage() {
            return image;
        }
    }
}
