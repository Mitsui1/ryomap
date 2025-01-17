package com.example.ryomap;

import android.content.Intent;

import android.os.Bundle;

import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;


public class Mypage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage);


        // ボタンのクリックリスナー
        Button backmap_btn = findViewById(R.id.button_logout);
        backmap_btn.setOnClickListener(v -> {
            Intent intent = new Intent(Mypage.this, Logout.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // 画面遷移のアニメーション削除
        });

        Button menu_back_Button = findViewById(R.id.button_back_menu);
        menu_back_Button.setOnClickListener(v -> {
            Intent intent = new Intent(Mypage.this, MenuActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // 画面遷移のアニメーション削除
        });

        // Mypage でメールアドレスを受け取る
        String userEmail = getIntent().getStringExtra("user_mail");
        Button button_account_Change = findViewById(R.id.button_account_change);
        button_account_Change.setOnClickListener(v -> {
            Intent intent = new Intent(Mypage.this, CheckAccountEdit.class);
            //intent.putExtra("user_mail", userEmail); // メールアドレスを渡す
            startActivity(intent);
            overridePendingTransition(0, 0); // 画面遷移のアニメーション削除
        });
    }

}
