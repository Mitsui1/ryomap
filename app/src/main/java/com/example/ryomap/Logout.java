package com.example.ryomap;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import android.widget.Button;



import androidx.appcompat.app.AppCompatActivity;



public class Logout extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.logout); //xmlファイルの選択



        // ログアウト画面からマイページへの遷移

        Button backmypage_btn = findViewById(R.id.button_back_logout); //画面遷移先のトリガーとなるボタン指定で

        backmypage_btn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                //画面遷移元と画面遷移先の指定

                Intent intent = new Intent(Logout.this, Mypage.class);

                startActivity(intent);

                overridePendingTransition(0, 0); //画面遷移のアニメーション削除

            }

        });



        // ログアウト画面からアプリ終了

        Button logout_btn = findViewById(R.id.button_logout); //画面遷移先のトリガーとなるボタン指定で

        logout_btn.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                //画面遷移元と画面遷移先の指定

                // Mypage.class を変更予定

                Intent intent = new Intent(Logout.this, MainActivity.class);

                startActivity(intent);

                overridePendingTransition(0, 0); //画面遷移のアニメーション削除

            }

        });



    }

}