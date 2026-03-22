package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.Controller.DepthController;
import com.example.myapplication.Controller.PHController;
import com.example.myapplication.Controller.TdsController;
import com.example.myapplication.Controller.TemperatureController;
import com.example.myapplication.Model.AlertManager;
import com.example.myapplication.Model.DepthData;
import com.example.myapplication.Model.PHData;
import com.example.myapplication.Model.TdsData;
import com.example.myapplication.Model.TemperatureData;
import com.example.myapplication.R;
import com.example.myapplication.UI.GaugeView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.view.View;
import com.example.myapplication.Model.MaintenanceTaskManager;

public class MainActivity extends AppCompatActivity {
    protected GaugeView temp_gauge, depth_gauge, ph_gauge, tds_gauge;

    private TemperatureController temperatureController;
    private PHController phController;
    private TdsController tdsController;
    private DepthController depthController;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());

    private double temp;
    private double ph;
    private double tds;
    private double depth;
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_COOLDOWN = 5 * 60 * 1000; // 5 minutes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }

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
        phController = new PHController();
        tdsController = new TdsController();
        depthController = new DepthController();

        refreshHandler.post(refreshRunnable);

        // --- MAINTENANCE TASKS LOGIC ---
        MaintenanceTaskManager.setupTask(this,
                findViewById(R.id.check_filter), findViewById(R.id.date_filter), findViewById(R.id.layout_filter), 21, "filter cleaning");

        MaintenanceTaskManager.setupTask(this,
                findViewById(R.id.check_skimmer), findViewById(R.id.date_skimmer), findViewById(R.id.layout_skimmer), 30, "skimmer clearing");

        MaintenanceTaskManager.setupTask(this,
                findViewById(R.id.check_pump), findViewById(R.id.date_pump), findViewById(R.id.layout_pump), 30, "pump inspection");

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


    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchTemperature();
            fetchPh();
            fetchTds();
            fetchDepth();

            refreshHandler.postDelayed(this, 30000);
        }
    };
    private void setupUI() {
        temp_gauge = findViewById(R.id.temp_gauge);
        temp_gauge.setTitle("Temperature");
        temp_gauge.setMinValue(0f);
        temp_gauge.setMaxValue(50f);
        temp_gauge.setRanges(10f, 18f, 30f, 38f);

        depth_gauge = findViewById(R.id.depth_gauge);
        depth_gauge.setTitle("Depth");
        depth_gauge.setMinValue(0f);
        depth_gauge.setMaxValue(200f);
        depth_gauge.setRanges(160f, 170f, 190f, 195f);
        depth_gauge.setValueAnimated(182f);

        ph_gauge = findViewById(R.id.ph_gauge);
        ph_gauge.setTitle("Ph");
        ph_gauge.setMinValue(0f);
        ph_gauge.setMaxValue(14f);
        ph_gauge.setRanges(2000f, 2800f, 6000f, 7000f);

        tds_gauge = findViewById(R.id.tds_gauge);
        tds_gauge.setTitle("TDS");
        tds_gauge.setMinValue(0f);
        tds_gauge.setMaxValue(8000f);
        tds_gauge.setRanges(10f, 18f, 30f, 38f);
        tds_gauge.setValueAnimated(2030f);
    }

    private void fetchTemperature() {
        String userId = "1";

        temperatureController.fetchTemperatureForUser(userId, new TemperatureController.TemperatureCallback() {
            @Override
            public void onSuccess(TemperatureData temperatureData) {
                //temp_textView.setText(temperatureData.getTemperature() + " °C");
                temp = temperatureData.getTemperature();
                temp_gauge.setValueAnimated((float) temp);
                checkAllDataReady();
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
        private void fetchPh() {
            String userId = "1";

            phController.fetchPhForUser(userId, new PHController.PHCallback() {
                @Override
                public void onSuccess(PHData phData) {
                    //temp_textView.setText(temperatureData.getTemperature() + " °C");
                    ph = phData.getPh();
                    ph_gauge.setValueAnimated((float) ph);
                    checkAllDataReady();
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

        private void fetchTds() {
        String userId = "1";


        tdsController.fetchTdsForUser(userId, new TdsController.TdsCallback() {
            @Override
            public void onSuccess(TdsData tdsData) {
                //temp_textView.setText(temperatureData.getTemperature() + " °C");
                tds = tdsData.getTds();
                tds_gauge.setValueAnimated((float) tds);
                checkAllDataReady();
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

        private void fetchDepth() {
        String userId = "1";

        depthController.fetchDepthForUser(userId, new DepthController.DepthCallback() {
            @Override
            public void onSuccess(DepthData depthData) {
                //temp_textView.setText(temperatureData.getTemperature() + " °C");
                depth = depthData.getDepth();
                depth_gauge.setValueAnimated((float) depth);
                checkAllDataReady();
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

    private void checkAllDataReady() {

        long currentTime = System.currentTimeMillis();

        if (currentTime - lastNotificationTime < NOTIFICATION_COOLDOWN) {
            return;
        }

        //float chlorine = tds * 0.0005f;
        //float salt (formula)

        boolean alertTriggered = AlertManager.checkLevels(this, 10, 2500, ph, depth, tds, temp);

        if (alertTriggered) {
            lastNotificationTime = currentTime;
        }
    }



}
