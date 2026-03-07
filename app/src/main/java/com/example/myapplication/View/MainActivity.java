package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Toast;
import com.example.myapplication.Model.ApiClient;
import com.example.myapplication.Model.ApiService;
import com.example.myapplication.Model.TemperatureData;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.myapplication.R;


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

    private void fetchTemperature() {
        String userId = "1";
        temp_textView.setText("Test °C");
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);

//        if (username == null || username.isEmpty()) {
//            temp_textView.setText("bug1");
//            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
//            return;
//        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.getTemperaturesForUser(userId).enqueue(new Callback<List<TemperatureData>>() {
            @Override
            public void onResponse(Call<List<TemperatureData>> call, Response<List<TemperatureData>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<TemperatureData> temperatures = response.body();

                    if (!temperatures.isEmpty()) {
                        TemperatureData latest = temperatures.get(temperatures.size() - 1);

                        temp_textView.setText(latest.getTemperature() + " °C");

                    } else {
                        temp_textView.setText("No data");
                    }

                } else {
                    Toast.makeText(MainActivity.this,
                            "Failed: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TemperatureData>> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
}}