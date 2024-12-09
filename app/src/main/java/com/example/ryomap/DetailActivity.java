package com.example.ryomap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail); //xmlファイルの選択

        Button backmap_btn = findViewById(R.id.backmap_btn); //画面遷移先のトリガーとなるボタン指定で
        backmap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移元と画面遷移先の指定
                Intent intent = new Intent(DetailActivity.this, MapActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); //画面遷移のアニメーション削除
            }
        });


    }
}