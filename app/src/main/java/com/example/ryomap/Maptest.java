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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

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
    private List<Marker> markers = new ArrayList<>();
    private boolean isInitialPositionSet = false;
    private List<MaterialButton> categoryButtons = new ArrayList<>();
    private ColorStateList defaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        Button backmenu_btn = findViewById(R.id.backmenu_btn);
        backmenu_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Maptest.this, MenuActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        zoomInBtn = findViewById(R.id.zoom_in_btn);
        zoomInBtn.setOnClickListener(v -> zoomIn());

        zoomOutBtn = findViewById(R.id.zoom_out_btn);
        zoomOutBtn.setOnClickListener(v -> zoomOut());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                onMapReady(googleMap);
            });
        }

        MaterialButton sightseeingButton = findViewById(R.id.sightseeingbutton);
        MaterialButton foodButton = findViewById(R.id.foodbutton);
        MaterialButton hotelButton = findViewById(R.id.hotelbutton);
        MaterialButton eventButton = findViewById(R.id.eventbutton);
        MaterialButton favoriteButton = findViewById(R.id.favoritebutton);
        MaterialButton secretButton = findViewById(R.id.secretbutton);

        categoryButtons.add(sightseeingButton);
        categoryButtons.add(foodButton);
        categoryButtons.add(hotelButton);
        categoryButtons.add(eventButton);
        categoryButtons.add(favoriteButton);
        categoryButtons.add(secretButton);

        defaultColor = ColorStateList.valueOf(Color.parseColor("#80FFFFFF"));

        sightseeingButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(1);
            changeButtonColor(sightseeingButton);  // 色変更
        });
        foodButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(2);
            changeButtonColor(foodButton);  // 色変更
        });
        hotelButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(3);
            changeButtonColor(hotelButton);  // 色変更
        });
        eventButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(4);
            changeButtonColor(eventButton);  // 色変更
        });
        favoriteButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(5);
            changeButtonColor(favoriteButton);  // 色変更
        });
        secretButton.setOnClickListener(v -> {
            fetchAndDisplaySpots(6);
            changeButtonColor(secretButton);  // 色変更
        });

    }

    private void changeButtonColor(MaterialButton selectedButton) {
        for (MaterialButton button : categoryButtons) {
            button.setBackgroundTintList(defaultColor);
        }
        selectedButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#80FFCC00")));
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (!isInitialPositionSet) {
            LatLng kochi = new LatLng(33.620, 133.719);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kochi, 10));
            isInitialPositionSet = true;
        }

        mMap.setOnMarkerClickListener(marker -> {
            Intent intent = new Intent(Maptest.this, DetailActivity.class);
            intent.putExtra("markerTitle", marker.getTitle());
            intent.putExtra("markerAddress", marker.getPosition().toString());
            if (marker.getTag() != null) {
                int spotId = (int) marker.getTag();
                intent.putExtra("spotId", spotId);
            } else {
                intent.putExtra("spotId", -1);
                //Log.d("Maptest", "spotId set to default: -1");
            }
            startActivity(intent);
            return true;
        });
    }

    private void fetchAndDisplaySpots(int kategoriId) {
        clearMarkers();
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
                    String query = "SELECT spot_id, spot_name, address, image FROM spot WHERE kategori_id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, kategoriId);

                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        int spotId = resultSet.getInt("spot_id");
                        String name = resultSet.getString("spot_name");
                        String address = resultSet.getString("address");
                        String imagePath = resultSet.getString("image");
                        spots.add(new Spot(spotId,kategoriId, name, address, imagePath));
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
                    geocodeAddressAndAddPin(spot.getName(), spot.getAddress(), spot.getSpotId(), spot.getKategoriId());
                }
            }
        }
    }

    private Bitmap resizeBitmap(int resourceId, int width, int height) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }


    //マップピンを追加する
    private void geocodeAddressAndAddPin(String name, String address, int spotId, int kategoriId) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (!addresses.isEmpty()) {
                Address location = addresses.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                LatLng latLng = new LatLng(latitude, longitude);
                BitmapDescriptor icon = getCategoryIcon(kategoriId);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .icon(icon));
                marker.setTag(spotId);

                markers.add(marker);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "住所の変換に失敗しました: " + address, Toast.LENGTH_SHORT).show();
        }
    }

    private BitmapDescriptor getCategoryIcon(int kategoriId){
        switch (kategoriId) {
            case 1:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            case 2:  // 食べ物
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            case 3:  // ホテル
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            case 4:  // イベント
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            case 5:  // お気に入り
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            case 6:  // 秘密の場所
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            default:  // デフォルトの色
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);

        }
    }

    private void clearMarkers() {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

    private void zoomIn() {
        if (mMap != null) {
            float zoomLevel = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, zoomLevel + 1));
        }
    }

    private void zoomOut() {
        if (mMap != null) {
            float zoomLevel = mMap.getCameraPosition().zoom;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMap.getCameraPosition().target, zoomLevel - 1));
        }
    }

    public static class Spot {
        private final int spotId;
        private final int kategoriId;
        private final String name;
        private final String address;
        private final String image;

        public Spot(int spotId, int kategoriId, String name, String address, String image) {
            this.spotId = spotId;
            this.kategoriId = kategoriId;
            this.name = name;
            this.address = address;
            this.image = image;
        }

        public int getSpotId() {
            return spotId;
        }

        public  int getKategoriId(){
            return kategoriId;
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
