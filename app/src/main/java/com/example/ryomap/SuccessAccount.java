package com.example.ryomap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessAccount extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.successaccount); //xmlファイルの選択

        Button successAccountlogin = findViewById(R.id.successaccountlogin); //画面遷移先のトリガーとなるボタン指定で
        successAccountlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(SuccessAccount.this, Mypage.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });


    }
}
