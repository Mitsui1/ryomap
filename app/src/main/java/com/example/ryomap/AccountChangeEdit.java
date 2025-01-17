package com.example.ryomap;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class AccountChangeEdit extends AppCompatActivity {

    private EditText passwordField, confirmPasswordField, userNameField, ageField;
    private RadioGroup genderGroup;
    private Button updateAccountButton, backButton;
    private String userEmail; // ログイン時に渡されたメールアドレス

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountchangeedit);

        userEmail = getIntent().getStringExtra("user_mail"); // 受け取ったメールアドレス

        passwordField = findViewById(R.id.password);
        confirmPasswordField = findViewById(R.id.re_password);
        userNameField = findViewById(R.id.under_name);
        ageField = findViewById(R.id.age);
        genderGroup = findViewById(R.id.gender_group);
        updateAccountButton = findViewById(R.id.create_account);
        backButton = findViewById(R.id.button_back);

        EditText emailField = findViewById(R.id.makeloginid);
        emailField.setText(userEmail);
        //emailField.setEnabled(false); // メールアドレスを編集不可にする

        backButton.setOnClickListener(v -> finish());

        updateAccountButton.setOnClickListener(v -> {
            updateAccountButton.setEnabled(false); // ボタンを無効化し、連続クリックを防ぐ
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();
            String userName = userNameField.getText().toString().trim();
            String ageStr = ageField.getText().toString().trim();

            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            String gender = "Other";
            if (selectedGenderButton != null) {
                String selectedGenderText = selectedGenderButton.getText().toString();
                gender = selectedGenderText.equals("男性") ? "M" :
                        selectedGenderText.equals("女性") ? "F" : "Other";
            }

            if (password.isEmpty() || confirmPassword.isEmpty() || userName.isEmpty() || ageStr.isEmpty()) {
                Toast.makeText(this, "すべての項目を入力してください", Toast.LENGTH_SHORT).show();
                updateAccountButton.setEnabled(true);
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "パスワードが一致しません", Toast.LENGTH_SHORT).show();
                updateAccountButton.setEnabled(true);
                return;
            }

            String newEmail = emailField.getText().toString().trim();
            new UpdateUserTask().execute(newEmail,password, userName, gender, ageStr);
        });
    }


    private class UpdateUserTask extends AsyncTask<String, Void, Boolean> {
        private String newEmail;
        @Override
        protected Boolean doInBackground(String... params) {
            newEmail = params[0];
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

                // 性別のバリデーション
                if (!(gender.equals("M") || gender.equals("F") || gender.equals("Other"))) {
                    return false;
                }

                // 年齢の変換とバリデーション
                int age = Integer.parseInt(ageStr); // ageStr を整数に変換
                if (age < 0 || age > 120) {
                    return false;
                }

                // データベース更新処理
                String query = "UPDATE `user` SET user_mail = ?, password = ?, user_name = ?, user_gender = ?, user_age = ? WHERE user_mail = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, newEmail);
                statement.setString(2, password);
                statement.setString(3, userName);
                statement.setString(4, gender);
                statement.setInt(5, age);
                statement.setString(6, userEmail); // ログイン時に渡されたメールアドレスで更新対象を指定

                int rowsUpdated = statement.executeUpdate();
                statement.close();
                connection.close();

                return rowsUpdated > 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                userEmail = newEmail; //変更後のメールアドレス更新
                Toast.makeText(AccountChangeEdit.this, "アカウント情報が更新されました", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AccountChangeEdit.this, SuccessAccount.class));
                finish(); // 更新完了後にアクティビティを終了
            } else {
                Toast.makeText(AccountChangeEdit.this, "アカウント情報の更新に失敗しました", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

