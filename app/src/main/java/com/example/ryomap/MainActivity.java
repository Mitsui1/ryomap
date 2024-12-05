package com.example.ryomap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //xmlファイルの選択

        Button button_login = findViewById(R.id.button_login); //画面遷移先のトリガーとなるボタン指定で
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });

        Button button_acccunt = findViewById(R.id.button_account); //画面遷移先のトリガーとなるボタン指定で
        button_acccunt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });
    }
}