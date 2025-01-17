package com.example.ryomap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class SpotSelect extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.spotselect);
        // スポット追加へ
        Button Button1 = findViewById(R.id.goSpotInsertButton);
        Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotSelect.this, SpotInsert.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // スポット編集へ
        Button Button2 = findViewById(R.id.goSpotEditButton);
        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotSelect.this, SpotEditList.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

        });

        // スポット削除へ
        Button Button3 = findViewById(R.id.goSpotDeleteButton);
        Button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotSelect.this, SpotDelete.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        // メニューへ
        Button Button4 = findViewById(R.id.menu_back_button);
        Button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotSelect.this, MenuActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}