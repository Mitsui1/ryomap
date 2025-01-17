package com.example.ryomap;



import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import android.widget.Button;



import androidx.appcompat.app.AppCompatActivity;



public class SpotDeleteSuccess extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);

        setContentView(R.layout.spotdeletesuccess);

        // メニューへ

        Button button = findViewById(R.id.menu_back_button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Intent intent = new Intent(SpotDeleteSuccess.this, MenuActivity.class);

                startActivity(intent);

                overridePendingTransition(0, 0);

            }

        });

    }

}