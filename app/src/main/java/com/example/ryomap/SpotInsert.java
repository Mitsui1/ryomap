package com.example.ryomap;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SpotInsert extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;

    private EditText inputKategoriId, inputSpotName, inputSpotIntro, inputBesiHours;
    private EditText inputPhoneNumber, inputAddress, inputUrl;
    private ImageView selectedImageView;

    private String imagePath = ""; // 保存した画像のパス

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spotinsert);

        // 各EditTextを取得
        inputKategoriId = findViewById(R.id.inputKategoriId);
        inputSpotName = findViewById(R.id.inputSpotName);
        inputSpotIntro = findViewById(R.id.inputSpotIntro);
        inputBesiHours = findViewById(R.id.inputBesiHours);
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        inputAddress = findViewById(R.id.inputAddress);
        inputUrl = findViewById(R.id.inputUrl);

        selectedImageView = findViewById(R.id.selectedImageView);

        Button button = findViewById(R.id.menu_back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotInsert.this, MenuActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // 画像選択ボタン
        Button selectImageButton = findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_PICK_CODE);
            }
        });

        // データ挿入ボタン
        Button insertSpotButton = findViewById(R.id.insertSpotButton);
        insertSpotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kategoriId = inputKategoriId.getText().toString();
                String spotName = inputSpotName.getText().toString();
                String spotIntro = inputSpotIntro.getText().toString();
                String besiHours = inputBesiHours.getText().toString();
                String phoneNumber = inputPhoneNumber.getText().toString();
                String address = inputAddress.getText().toString();
                String url = inputUrl.getText().toString();

                new InsertSpotTask().execute(kategoriId, spotName, spotIntro, besiHours, phoneNumber, address, imagePath, url);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                // 画像を読み込む
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                selectedImageView.setImageBitmap(bitmap);

                // 画像をローカルストレージに保存
                imagePath = saveImageToStorage(bitmap);

            } catch (Exception e) {
                Toast.makeText(this, "画像の読み込みに失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 画像をストレージに保存し、そのパスを返す
    private String saveImageToStorage(Bitmap bitmap) {
        String storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MyAppImages";
        File dir = new File(storageDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 一意のファイル名を生成
        String fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg";
        File file = new File(dir, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    // 非同期タスクでデータベースに挿入
    private class InsertSpotTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                // JDBCドライバのロード
                Class.forName("org.mariadb.jdbc.Driver");

                // データベースに接続
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
                String user = "root";
                String password = "h11y24n20";
                connection = DriverManager.getConnection(url, user, password);

                if (connection != null) {
                    // SQLクエリを準備
                    String sql = "INSERT INTO SPOT (kategori_id, spot_name, spot_intro, besi_hours, phone_number, address, image, url) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    preparedStatement = connection.prepareStatement(sql);

                    // パラメータを設定
                    preparedStatement.setInt(1, Integer.parseInt(params[0])); // kategori_id
                    preparedStatement.setString(2, params[1]); // spot_name
                    preparedStatement.setString(3, params[2]); // spot_intro
                    preparedStatement.setString(4, params[3]); // besi_hours
                    preparedStatement.setString(5, params[4]); // phone_number
                    preparedStatement.setString(6, params[5]); // address
                    preparedStatement.setString(7, params[6]); // 画像のパスを保存
                    preparedStatement.setString(8, params[7]); // url

                    int rowsAffected = preparedStatement.executeUpdate();
                    result = rowsAffected > 0 ? "データ挿入成功" : "データ挿入失敗";
                } else {
                    result = "データベース接続失敗";
                }

            } catch (ClassNotFoundException e) {
                result = "JDBCドライバが見つかりません: " + e.getMessage();
            } catch (SQLException e) {
                result = "データベースエラー: " + e.getMessage();
            } finally {
                try {
                    if (preparedStatement != null) preparedStatement.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(SpotInsert.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
