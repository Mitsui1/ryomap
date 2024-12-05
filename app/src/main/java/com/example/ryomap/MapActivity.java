package com.example.ryomap;

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

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button back_btn, zoomInBtn, zoomOutBtn;

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

        // 戻るボタンの設定
        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(v -> onBackPressed());

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
        LatLng tokyo = new LatLng(33.620, 133.719);  // 例: 高知工科大学
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, 13));  // ズームレベル13
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
