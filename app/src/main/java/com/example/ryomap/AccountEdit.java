package com.example.ryomap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountEdit extends AppCompatActivity {

    private EditText emailField, passwordField, confirmPasswordField, userNameField, ageField;
    private RadioGroup genderGroup;
    private Button createAccountButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountedit);

        emailField = findViewById(R.id.makeloginid);
        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.re_password);
        userNameField = findViewById(R.id.under_name);
        ageField = findViewById(R.id.age);
        genderGroup = findViewById(R.id.gender_group);
        createAccountButton = findViewById(R.id.create_account);
        backButton = findViewById(R.id.button_back);

        // 戻るボタン
        backButton.setOnClickListener(v -> finish());

        // アカウント作成ボタン
        createAccountButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();
            String userName = userNameField.getText().toString().trim();
            String ageStr = ageField.getText().toString().trim();

            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            // 性別のマッピング処理
            String gender = "Other";  // デフォルト値を "Other" に設定
            if (selectedGenderButton != null) {
                String selectedGenderText = selectedGenderButton.getText().toString();
                if ("男性".equals(selectedGenderText)) {
                    gender = "M";  // "男性" → "M"
                } else if ("女性".equals(selectedGenderText)) {
                    gender = "F";  // "女性" → "F"
                } else if ("その他".equals(selectedGenderText)) {
                    gender = "Other";  // "その他" → "Other"
                }
            }

            // 入力チェック
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || userName.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(AccountEdit.this, "すべての項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(AccountEdit.this, "パスワードが一致しません", Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age < 0 || age > 120) {
                    Toast.makeText(AccountEdit.this, "年齢は0～120の範囲で入力してください", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(AccountEdit.this, "年齢は数値で入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            // データベース登録
            new RegisterUserTask().execute(email, password, userName, gender, String.valueOf(age));
        });
    }

    private class RegisterUserTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String email = params[0];
            String password = params[1];
            String userName = params[2];
            String gender = params[3];
            String ageStr = params[4];

            try {
                Class.forName("org.mariadb.jdbc.Driver");
                String url = "jdbc:mariadb://10.0.2.2:3306/test_db";
                String dbUser = "root";
                String dbPassword = "h11y24n20";
                Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
                Log.d("DB_CONNECTION", "接続成功！");

                // メールアドレスの重複チェック
                String checkQuery = "SELECT COUNT(*) FROM user WHERE user_mail = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, email);
                ResultSet rs = checkStatement.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // ここでは Toast を表示しない
                    rs.close();
                    checkStatement.close();
                    return false;
                }

                // 性別のバリデーション
                if (!(gender.equals("M") || gender.equals("F") || gender.equals("Other"))) {
                    return false;
                }

                // 年齢の変換とバリデーション
                int age = 0;
                try {
                    age = Integer.parseInt(ageStr); // ageStr を整数に変換
                    if (age < 0 || age > 120) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }

                // データベース挿入処理
                String query = "INSERT INTO `user` (user_mail, password, user_name, user_gender, user_age) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, password);
                statement.setString(3, userName);
                statement.setString(4, gender);
                statement.setInt(5, age);

                int rowsInserted = statement.executeUpdate();
                statement.close();
                connection.close();

                return rowsInserted > 0;
            } catch (Exception e) {
                Log.e("DB_ERROR", "データベースエラー", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // UI スレッドでのトースト表示
            if (result) {
                startActivity(new Intent(AccountEdit.this,SuccessAccount.class));
                finish();  // AccountEdit アクティビティを終了して戻る
            } else {
                Toast.makeText(AccountEdit.this, "アカウント作成に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }
}



