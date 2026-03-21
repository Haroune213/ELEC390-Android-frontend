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
import com.example.myapplication.UI.GaugeView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    protected GaugeView temp_gauge, depth_gauge, ph_gauge, tds_gauge;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();  // get the ID of the selected item
            if (id == R.id.nav_home) {
                // Already on MainActivity
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });


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
        temp_gauge = findViewById(R.id.temp_gauge);
        temp_gauge.setTitle("Temperature");
        temp_gauge.setMinValue(0f);
        temp_gauge.setMaxValue(50f);
        temp_gauge.setRanges(10f, 18f, 30f, 38f);
        temp_gauge.setValueAnimated(22f);

        temp_gauge = findViewById(R.id.depth_gauge);
        temp_gauge.setTitle("Depth");
        temp_gauge.setMinValue(0f);
        temp_gauge.setMaxValue(200f);
        temp_gauge.setRanges(160f, 170f, 190f, 195f);
        temp_gauge.setValueAnimated(182f);

        temp_gauge = findViewById(R.id.ph_gauge);
        temp_gauge.setTitle("Ph");
        temp_gauge.setMinValue(0f);
        temp_gauge.setMaxValue(14f);
        temp_gauge.setRanges(2000f, 2800f, 6000f, 7000f);
        temp_gauge.setValueAnimated(8f);
        temp_gauge = findViewById(R.id.tds_gauge);
        temp_gauge.setTitle("TDS");
        temp_gauge.setMinValue(0f);
        temp_gauge.setMaxValue(8000f);
        temp_gauge.setRanges(10f, 18f, 30f, 38f);
        temp_gauge.setValueAnimated(2030f);
//        chlorine_button = findViewById(R.id.chlorine_button);
//        pH_button = findViewById(R.id.pH_button);
//        wlvl_button = findViewById(R.id.wlvl_button);
//        temp_button = findViewById(R.id.temp_button);
//
//        chlorine_textView = findViewById(R.id.chlorine_textView);
//        pH_textView = findViewById(R.id.pH_textView);
//        wlvl_textView = findViewById(R.id.wlvl_textView);
//        temp_textView = findViewById(R.id.temp_textView);
    }

    private void fetchTemperature() {
        String userId = "1";

        temperatureController.fetchTemperatureForUser(userId, new TemperatureController.TemperatureCallback() {
            @Override
            public void onSuccess(TemperatureData temperatureData) {
                //temp_textView.setText(temperatureData.getTemperature() + " °C");
                double temp = temperatureData.getTemperature();
                temp_gauge.setValue((float) temp);
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}