package com.example.ryomap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu); //xmlファイルの選択



        Button button = findViewById(R.id.map); //画面遷移先のトリガーとなるボタン指定で
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(MenuActivity.this, Maptest.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });

        Button button_zititai = findViewById(R.id.zititai_button); //画面遷移先のトリガーとなるボタン指定で
        button_zititai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(MenuActivity.this, SpotInsert.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });

        // Login から渡されたメールアドレスを受け取る
        // LoginActivity から渡されたメールアドレスを受け取る
        String userEmail = getIntent().getStringExtra("user_mail");
        Button mypage_Button = findViewById(R.id.mypage_button);
        mypage_Button.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, Mypage.class);
            //intent.putExtra("user_mail", userEmail); // メールアドレスを渡す
            startActivity(intent);
            overridePendingTransition(0, 0); // 画面遷移のアニメーション削除
        });


    }
}