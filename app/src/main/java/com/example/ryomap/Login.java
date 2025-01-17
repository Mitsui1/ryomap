package com.example.ryomap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        EditText inputAdress = findViewById(R.id.inputAdress);
        EditText inputPass = findViewById(R.id.inputPass);
        Button submitButton = findViewById(R.id.submitButton);
        Button backButton = findViewById(R.id.button_back);

        // 戻るボタン
        backButton.setOnClickListener(v -> finish()); // 現在の画面を閉じて、前の画面に戻る

        // ログインボタン
        submitButton.setOnClickListener(v -> {
            String userInputAdress = inputAdress.getText().toString();
            String userInputPass = inputPass.getText().toString();

            if (!userInputAdress.isEmpty() && !userInputPass.isEmpty()) {
                // 非同期でログインチェック
                new LoginTask().execute(userInputAdress, userInputPass);
            } else {
                Toast.makeText(Login.this, "文字を入力してください。", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 非同期タスクでデータベース接続
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userInputAdress = params[0];
            String userInputPass = params[1];
            String userEmail = null;

            try {
                // JDBCドライバーのロード
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";  // データベースの接続情報
                String user = "root";
                String password = "h11y24n20";
                Connection connection = DriverManager.getConnection(url, user, password);

                // SQLクエリ
                String query = "SELECT * FROM `user` WHERE user_mail = ? AND password = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, userInputAdress);
                statement.setString(2, userInputPass);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    // ログイン成功時にメールアドレスを取得
                    userEmail = resultSet.getString("user_mail");
                }

                resultSet.close();
                statement.close();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
                // UIスレッドでエラー通知
                runOnUiThread(() -> Toast.makeText(Login.this, "データベース接続エラー: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
            return userEmail;  // メールアドレスを返す
        }

        @Override
        protected void onPostExecute(String userEmail) {
            super.onPostExecute(userEmail);
            if (userEmail != null) {
                // ログイン成功時、メールアドレスを次の画面（MenuActivity）に渡す
                Intent intent = new Intent(Login.this, MenuActivity.class);
                //intent.putExtra("user_mail", userEmail);  // 実際に取得したメールアドレスを渡す
                startActivity(intent);
            } else {
                Toast.makeText(Login.this, "ログイン失敗: メールアドレスまたはパスワードが違います", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
