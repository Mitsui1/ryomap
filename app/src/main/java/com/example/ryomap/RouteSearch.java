package com.example.ryomap;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.URL;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLHandshakeException;

public class RouteSearch extends AppCompatActivity implements OnMapReadyCallback {
    public GoogleMap mMap;
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    private Button zoomInBtn, zoomOutBtn;
    // マーカーを管理するリスト
    private List<Marker> markers = new ArrayList<>();
    // 初期位置が設定されたかどうかを管理するフラグ
    private boolean isInitialPositionSet = false;
    // ボタンのリストを作成
    private List<MaterialButton> categoryButtons = new ArrayList<>();
    private ColorStateList defaultColor; // ボタンのデフォルト背景色
    private String apikey = BuildConfig.MAPS_API_KEY;
    private String org_markerAddress;
    private String org_markerTitle;
    private String dst_Address;
    private boolean mapreset = true;
    private Polyline polyline;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routesearch);
        Intent intent = getIntent();
        org_markerAddress = intent.getStringExtra("markerAddress");
        org_markerTitle = intent.getStringExtra("markerTitle");
        Log.d("markerT=", org_markerTitle);
        Log.d("markerA=", org_markerAddress);
        Button backmenu_btn = findViewById(R.id.backmenu_btn); //画面遷移先のトリガーとなるボタン指定で
        backmenu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(RouteSearch.this, DetailActivity.class);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapreset = true;
        mMap.clear();
        // 初期位置が設定されていない場合のみ設定
        if (!isInitialPositionSet) {
            // 初期位置の設定
            LatLng kochi = new LatLng(33.620, 133.719);  // 例: 高知工科大学
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kochi, 10));  // ズームレベル10
            isInitialPositionSet = true;  // 初期位置が設定されたことを記録
        }
        // Intent で受け取ったデータを直接 LatLng に変換してマーカーを追加
        if (org_markerAddress != null && !org_markerAddress.isEmpty()) {
            try {
                // "33.49696000000001,133.5736653" のような形式を LatLng に変換
                String[] latLngParts = org_markerAddress.replace("lat/lng: (", "").replace(")", "").split(",");
                double latitude = Double.parseDouble(latLngParts[0].trim());
                double longitude = Double.parseDouble(latLngParts[1].trim());
                LatLng orgLatLng = new LatLng(latitude, longitude);
                // マーカーを追加
                mMap.addMarker(new MarkerOptions().position(orgLatLng).title(org_markerTitle));
                // マーカーの位置にカメラを移動
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(orgLatLng, 12));
            } catch (Exception e) {
                Toast.makeText(this, "緯度経度の変換に失敗しました: " + org_markerAddress, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        // マーカークリックリスナーの設定
        mMap.setOnMarkerClickListener(marker -> {
            if (!mapreset) {
                onMapReady(mMap);
                mapreset = false;
            }
            dst_Address = marker.getPosition().toString();
            exroute();
            return true; // イベント消費
        });
    }
    
    private void fetchAndDisplaySpots(int kategoriId) {
        clearMarkers();  // 新しいスポットを表示する前に既存のマーカーを削除
        new FetchSpotsTask().execute(kategoriId);
    }

    // ピンを追加るメソッド
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
                // 追加したマーカーを markers リストに保存
                markers.add(marker);  // リストに追加する
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "住所の変換に失敗しました: " + address, Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng changegeocode(String markerAddress) {
        try {
                String[] latLngParts = markerAddress.replace("lat/lng: (", "").replace(")", "").split(",");
                double latitude = Double.parseDouble(latLngParts[0].trim());
                double longitude = Double.parseDouble(latLngParts[1].trim());
                LatLng latLng = new LatLng(latitude, longitude);
                Log.d("Geocoder", "変換成功: " + latitude + ", " + longitude); // 変換成功のログ
                return latLng;
            } else {
                Log.e("Geocoder", "住所が見つかりませんでした: " + markerAddress);
            }
        } catch (IOException e) {
            Log.e("Geocoder", "住所変換中にエラーが発生", e);
        }
        Toast.makeText(this, "住所の変換に失敗しました: " + markerAddress, Toast.LENGTH_SHORT).show();
        return null;
    }


    public void exroute() {
        //ここでsqlを操作して出発地点と目的地を決める
        
        LatLng org = new LatLng(changegeocode(org_markerAddress));
        LatLng dst = new LatLng(changegeocode(dst_Address));
        Log.d("org=", org.toString());
        Log.d("dst=", dst.toString());
        String url = getURL(org,dst);
        new MyAsync(this, mMap, polyline).execute(url);
    }

    //スタート地点と目標地点の座標を引数にもち，direction api のURLを返す
    private String getURL(LatLng org_latLng, LatLng dst_latLng) {
        String urlstr = "https://maps.googleapis.com/maps/api/directions/json?" +
                "destination=" + dst_latLng.latitude + "," + dst_latLng.longitude +
                "&origin=" + org_latLng.latitude + "," + org_latLng.longitude +
                "&mode=" + "driving" +
//                "&waypoints=" + waypoints +
                "&key=" + apikey;
        return urlstr;
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
        public String getPosition(){
            return getPosition();
        }
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
                Toast.makeText(RouteSearch.this, "該当するスポットが見つかりませんでした", Toast.LENGTH_SHORT).show();
            } else {
                for (Spot spot : spots) {
                    geocodeAddressAndAddPin(spot.getName(), spot.getAddress());
                }
            }
        }
    }
}
