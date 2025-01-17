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
    private boolean isFavorite = false;  // 現在の状態を管理
    // markerTitleとmarkerAddressをクラス内で定義
    private String markerTitle;
    private String markerAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail); // レイアウトの設定

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
        String markerTitle = intent.getStringExtra("markerTitle");
        String markerAddress = intent.getStringExtra("markerAddress");
        // 受け取ったデータをビューにセット
        nameTextView.setText(markerTitle);

        // データベースから追加情報を取得
        new FetchSpotDetailsTask().execute(markerTitle);

        Button backmenu_btn = findViewById(R.id.backmap_btn); //画面遷移先のトリガーとなるボタン指定で
        backmenu_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(DetailActivity.this, Maptest.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });

        Button route_button = findViewById(R.id.root_btn); //画面遷移先のトリガーとなるボタン指定で
        route_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(DetailActivity.this, RouteSearch.class);
                intent.putExtra("markerTitle", markerTitle);  // markerTitle を渡す
                intent.putExtra("markerAddress", markerAddress);  // markerAddress を渡す
                Log.d("markerT=", markerTitle);
                Log.d("markerA=", markerAddress);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });

        addFavorite = findViewById(R.id.addfavorite);
        normalFavorite = findViewById(R.id.normalfavorite);
        Button toggleButton = findViewById(R.id.addfavoriteButton);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 状態を切り替える
                if (isFavorite) {
                    addFavorite.setVisibility(View.GONE);  // 削除状態の画像を隠す
                    normalFavorite.setVisibility(View.VISIBLE);  // 追加状態の画像を表示
                } else {
                    addFavorite.setVisibility(View.VISIBLE);  // 削除状態の画像を表示
                    normalFavorite.setVisibility(View.GONE);  // 追加状態の画像を隠す
                }
                isFavorite = !isFavorite;  // 状態を切り替える
            }
        });


    }

    private class FetchSpotDetailsTask extends AsyncTask<String, Void, Spot> {

        @Override
        protected Spot doInBackground(String... params) {
            String spotName = params[0];
            Spot spot = null;

            try {
                // データベース接続
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";  // データベースの接続情報
                String user = "root";
                String password = "h11y24n20";

                Connection connection = DriverManager.getConnection(url, user, password);

                if (connection != null) {
                    String query = "SELECT spot_name, spot_intro, besi_hours, phone_number, address, image, url FROM SPOT WHERE spot_name = ?";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, spotName);

                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        spot = new Spot(
                                resultSet.getString("spot_name"),
                                resultSet.getString("spot_intro"),
                                resultSet.getString("besi_hours"),
                                resultSet.getString("phone_number"),
                                resultSet.getString("address"),
                                resultSet.getString("image"),  // imageに変更
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
                // UIに表示する情報を設定
                introTextView.setText(spot.getSpotIntro());
                hoursTextView.setText(spot.getBesiHours());
                phoneTextView.setText(spot.getPhoneNumber());
                addressTextView.setText(spot.getAddress());
                urlTextView.setText(spot.getUrl());

                // 画像の設定
                String imagePath = spot.getImage();  // imageに変更

                if (imagePath != null && !imagePath.isEmpty()) {
                    if (imagePath.startsWith("http")) {
                        // 画像のURLが指定されている場合、Picassoで画像を読み込む
                        Picasso.get().load(imagePath).into(pictureImageView);
                    } else {
                        // パスが指定されている場合、そのパスを使って画像を読み込む
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath); // ファイルパスから画像を読み込む
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

    // スポットの情報を保持するモデルクラス
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
            this.image = image;  // pictureからimageに変更
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getSpotIntro() {
            return spotIntro;
        }

        public String getBesiHours() {
            return besiHours;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getAddress() {
            return address;
        }

        public String getImage() {
            return image;  // pictureからimageに変更
        }

        public String getUrl() {
            return url;
        }
    }
}
