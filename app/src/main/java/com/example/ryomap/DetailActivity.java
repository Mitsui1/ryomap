package com.example.ryomap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DetailActivity extends AppCompatActivity {

    private TextView nameTextView, introTextView, hoursTextView, phoneTextView, addressTextView, urlTextView;
    private ImageView pictureImageView;
    private ImageView addFavorite, normalFavorite;
    private boolean isFavorite = false;
    private String markerTitle;
    private String markerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        // UI要素の初期化
        nameTextView = findViewById(R.id.nameTextView);
        introTextView = findViewById(R.id.introTextView);
        hoursTextView = findViewById(R.id.hoursTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        addressTextView = findViewById(R.id.addressTextView);
        urlTextView = findViewById(R.id.urlTextView);
        pictureImageView = findViewById(R.id.pictureImageView);

        // Intentからデータを受け取る
        Intent intent = getIntent();
        markerTitle = intent.getStringExtra("markerTitle");
        markerAddress = intent.getStringExtra("markerAddress");
        int spotId = intent.getIntExtra("spotId", -1);
        //Log.d("DetailActivity", "Received spotId: " + spotId);
        // 受け取ったデータをビューにセット
        nameTextView.setText(markerTitle);

        // データベースから追加情報を取得
        new FetchSpotDetailsTask().execute(spotId);

        Button backmenu_btn = findViewById(R.id.backmap_btn);
        backmenu_btn.setOnClickListener(v -> {
            Intent intent1 = new Intent(DetailActivity.this, Maptest.class);
            startActivity(intent1);
            overridePendingTransition(0, 0);
        });

        Button route_button = findViewById(R.id.root_btn);
        route_button.setOnClickListener(v -> {
            Intent intent1 = new Intent(DetailActivity.this, RouteSearch.class);
            intent1.putExtra("markerTitle", markerTitle);
            intent1.putExtra("markerAddress", markerAddress);
            Log.d("markerT=", markerTitle);
            Log.d("markerA=", markerAddress);
            startActivity(intent1);
            overridePendingTransition(0, 0);
        });

        addFavorite = findViewById(R.id.addfavorite);
        normalFavorite = findViewById(R.id.normalfavorite);
        Button toggleButton = findViewById(R.id.addfavoriteButton);

        toggleButton.setOnClickListener(v -> {
            if (isFavorite) {
                addFavorite.setVisibility(View.GONE);
                normalFavorite.setVisibility(View.VISIBLE);
            } else {
                addFavorite.setVisibility(View.VISIBLE);
                normalFavorite.setVisibility(View.GONE);
            }
            isFavorite = !isFavorite;
        });
    }

    private class FetchSpotDetailsTask extends AsyncTask<Integer, Void, Spot> {

        @Override
        protected Spot doInBackground(Integer... params) {
            int spotId = params[0]; // 受け取った spotId を使用
            Spot spot = null;

            try {
                // データベース接続
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";
                String user = "root";
                String password = "h11y24n20";

                Connection connection = DriverManager.getConnection(url, user, password);

                if (connection != null) {
                    String query = "SELECT spot_name, spot_intro, besi_hours, phone_number, address, image, url FROM spot WHERE spot_id = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setInt(1, spotId);

                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        spot = new Spot(
                                resultSet.getString("spot_name"),
                                resultSet.getString("spot_intro"),
                                resultSet.getString("besi_hours"),
                                resultSet.getString("phone_number"),
                                resultSet.getString("address"),
                                resultSet.getString("image"),
                                resultSet.getString("url")
                        );
                    }

                    resultSet.close();
                    statement.close();
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "データベースの接続に失敗しました", Toast.LENGTH_SHORT).show());
            }

            return spot;
        }

        @Override
        protected void onPostExecute(Spot spot) {
            super.onPostExecute(spot);

            if (spot != null) {
                introTextView.setText(spot.getSpotIntro());
                hoursTextView.setText(spot.getBesiHours());
                phoneTextView.setText(spot.getPhoneNumber());
                addressTextView.setText(spot.getAddress());
                urlTextView.setText(spot.getUrl());

                String imagePath = spot.getImage();

                if (imagePath != null && !imagePath.isEmpty()) {
                    if (imagePath.startsWith("http")) {
                        Picasso.get().load(imagePath).into(pictureImageView);
                    } else {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            pictureImageView.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            Toast.makeText(DetailActivity.this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            } else {
                Toast.makeText(DetailActivity.this, "スポットの詳細情報が見つかりませんでした", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class Spot {
        private final String name;
        private final String spotIntro;
        private final String besiHours;
        private final String phoneNumber;
        private final String address;
        private final String image;
        private final String url;

        public Spot(String name, String spotIntro, String besiHours, String phoneNumber, String address, String image, String url) {
            this.name = name;
            this.spotIntro = spotIntro;
            this.besiHours = besiHours;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.image = image;
            this.url = url;
        }

        public String getName() { return name; }
        public String getSpotIntro() { return spotIntro; }
        public String getBesiHours() { return besiHours; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getAddress() { return address; }
        public String getImage() { return image; }
        public String getUrl() { return url; }
    }
}
