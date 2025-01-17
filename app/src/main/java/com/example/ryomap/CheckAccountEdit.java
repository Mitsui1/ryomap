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

public class CheckAccountEdit extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button checkAccountButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkaccountedit);

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        checkAccountButton = findViewById(R.id.check_account);
        backButton = findViewById(R.id.button_back);

        // 戻るボタン
        backButton.setOnClickListener(v -> finish());

        // 照合ボタン
        checkAccountButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(CheckAccountEdit.this, "メールアドレスとパスワードを入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            new CheckUserTask().execute(email, password);
        });
    }

    private class CheckUserTask extends AsyncTask<String, Void, Boolean> {
        private String email;

        @Override
        protected Boolean doInBackground(String... params) {
            email = params[0];
            String password = params[1];

            try {
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";
                String dbUser = "root";
                String dbPassword = "h11y24n20";
                Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);

                String query = "SELECT COUNT(*) FROM `user` WHERE user_mail = ? AND password = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, password);

                ResultSet resultSet = statement.executeQuery();
                boolean exists = false;
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    exists = true;
                }

                resultSet.close();
                statement.close();
                connection.close();

                return exists;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(CheckAccountEdit.this, "認証成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CheckAccountEdit.this, AccountChangeEdit.class);
                intent.putExtra("user_mail", email);
                startActivity(intent);
                finish(); // 認証成功後、この画面を終了
            } else {
                Toast.makeText(CheckAccountEdit.this, "認証失敗: メールアドレスまたはパスワードが間違っています", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
