package com.example.ryomap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button zoomInBtn, zoomOutBtn, sightseeingButton, foodButton;
    private boolean pinsadd = false;

    double[] position_x = new double[]{33.6, 33.7, 33.8};
    double[] position_y = new double[]{133.7, 133.65, 133.5};
    String[] position_name = new String[]{"test", "test2","test3"};

    Marker[] markers = new Marker[position_name.length];

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        // Google Mapの準備
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // カテゴリー別ボタンの設定
        sightseeingButton = findViewById(R.id.sightseeingbutton);
        sightseeingButton.setOnClickListener(v -> addSightseeingPins());

        foodButton = findViewById(R.id.foodbutton);
        foodButton.setOnClickListener(v -> addFoodPins());

        Button backmenu_btn = findViewById(R.id.backmenu_btn); //画面遷移先のトリガーとなるボタン指定で
        backmenu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
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


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 初期位置の設定
        LatLng kochi = new LatLng(33.620, 133.719);  // 例: 高知工科大学
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kochi, 11));  // ズームレベル11
    }

    // 観光地のピンを追加する処理
    public void addSightseeingPins() {
        if (pinsadd) {
            pinsadd = false;
            mMap.clear();
        }
        for (int i = 0; i < position_name.length; i++) {
            if (mMap != null && !pinsadd) {
                // 高知工科大学
                LatLng location1 = new LatLng(position_x[i], position_y[i]);
                markers[i] = mMap.addMarker(new MarkerOptions()
                        .position(location1)
                        .title(position_name[i]));
                // テスト用観光地
//                LatLng location2 = new LatLng(33.5, 133.8);
//                Marker marker2 = mMap.addMarker(new MarkerOptions()
//                        .position(location2)
//                        .title("観光地2"));
//             マーカークリックイベントの設定
                mMap.setOnMarkerClickListener(clickedMarker -> {
                    for(int j = 0; j < position_name.length; j++) {
                        if (clickedMarker.equals(markers[j])) {
                            Intent intent = new Intent(MapActivity.this, DetailActivity.class);
                            //intent.putExtra("PLACE_NAME", "高知工科大学");
                            //intent.putExtra("PLACE_DESCRIPTION", "高知県の技術系大学で、最新の研究施設を備えています。");
                            startActivity(intent);
                            return true;
                        }
//                    if (clickedMarker.equals(marker)) {
//                        Intent intent = new Intent(MapActivity.this, DetailActivity.class);
//                        intent.putExtra("PLACE_NAME", "観光地2");
//                        intent.putExtra("PLACE_DESCRIPTION", "観光地2の詳細情報をここに記載してください。");
//                        startActivity(intent);
//                        return true;
//                    }
                    }
                        return false;

                });

            }
        }
        pinsadd = true; // ピンが追加されたことを記録
    }


    // 飲食店のピンを追加する処理
    private void addFoodPins() {
        if(pinsadd){
            pinsadd = false;
            mMap.clear();
        }
        if (mMap != null && !pinsadd) {
            // テスト
            LatLng location1 = new LatLng(33.61, 133.7);
            Marker marker1 = mMap.addMarker(new MarkerOptions()
                    .position(location1)
                    .title("高知工科大学"));

            // テスト用観光地
            LatLng location2 = new LatLng(33.69, 133.71);
            Marker marker2 = mMap.addMarker(new MarkerOptions()
                    .position(location2)
                    .title("test2"));

            // マーカークリックイベントの設定
            mMap.setOnMarkerClickListener(clickedMarker -> {
                if (clickedMarker.equals(marker1)) {
                    Intent intent = new Intent(MapActivity.this, DetailActivity.class);
                    //intent.putExtra("PLACE_NAME", "高知工科大学");
                    //intent.putExtra("PLACE_DESCRIPTION", "高知県の技術系大学で、最新の研究施設を備えています。");
                    startActivity(intent);
                    return true;
                }
                if (clickedMarker.equals(marker2)) {
                    Intent intent = new Intent(MapActivity.this, DetailActivity.class);
                    //intent.putExtra("PLACE_NAME", "観光地2");
                    //intent.putExtra("PLACE_DESCRIPTION", "観光地2の詳細情報をここに記載してください。");
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            pinsadd = true; // ピンが追加されたことを記録
        }
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


}
