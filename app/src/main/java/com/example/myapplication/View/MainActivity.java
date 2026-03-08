package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Controller.TemperatureController;
import com.example.myapplication.Model.TemperatureData;
import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {

    protected Button chlorine_button, pH_button, wlvl_button, temp_button;
    protected TextView chlorine_textView, pH_textView, wlvl_textView, temp_textView;

    private TemperatureController temperatureController;

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

        temperatureController = new TemperatureController();

        fetchTemperature();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setupUI() {
        chlorine_button = findViewById(R.id.chlorine_button);
        pH_button = findViewById(R.id.pH_button);
        wlvl_button = findViewById(R.id.wlvl_button);
        temp_button = findViewById(R.id.temp_button);

        chlorine_textView = findViewById(R.id.chlorine_textView);
        pH_textView = findViewById(R.id.pH_textView);
        wlvl_textView = findViewById(R.id.wlvl_textView);
        temp_textView = findViewById(R.id.temp_textView);
    }

    private void fetchTemperature() {
        String userId = "1";

        temperatureController.fetchTemperatureForUser(userId, new TemperatureController.TemperatureCallback() {
            @Override
            public void onSuccess(TemperatureData temperatureData) {
                temp_textView.setText(temperatureData.getTemperature() + " °C");
            }

            @Override
            public void onEmpty() {
                temp_textView.setText("No data");
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}