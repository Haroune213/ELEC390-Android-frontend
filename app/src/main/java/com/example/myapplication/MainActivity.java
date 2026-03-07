package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    protected Button chlorine_button, pH_button, wlvl_button, temp_button;
    protected TextView chlorine_textView, pH_textView, wlvl_textView, temp_textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupUI() {

        // Buttons init
        chlorine_button = findViewById(R.id.chlorine_button);
        pH_button = findViewById(R.id.pH_button);
        wlvl_button = findViewById(R.id.wlvl_button);
        temp_button = findViewById(R.id.temp_button);

        // Buttons action (to be added)

        // View init
        chlorine_textView = findViewById(R.id.chlorine_textView);
        pH_textView = findViewById(R.id.pH_textView);
        wlvl_textView = findViewById(R.id.wlvl_textView);
        temp_textView = findViewById(R.id.temp_textView);

        // View content (to be added)

    }
}