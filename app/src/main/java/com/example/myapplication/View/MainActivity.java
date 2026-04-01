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
import com.example.myapplication.Model.DepthData;
import com.example.myapplication.Model.NotificationHelper;
import com.example.myapplication.Model.PHData;
import com.example.myapplication.Model.TdsData;
import com.example.myapplication.Model.TemperatureData;
import com.example.myapplication.R;
import com.example.myapplication.UI.GaugeView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.Model.MaintenanceTaskManager;

public class MainActivity extends AppCompatActivity {
    protected GaugeView temp_gauge, depth_gauge, ph_gauge, tds_gauge;

    private TemperatureController temperatureController;
    private PHController phController;
    private TdsController tdsController;
    private DepthController depthController;
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());

    private String currentUserId;

    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHandler.post(refreshRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            currentUserId = prefs.getString("userId", "user123");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. CRÉER LE CANAL EN PRIORITÉ HAUTE IMMÉDIATEMENT
        NotificationHelper.createAlertChannel(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 101);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_profile) {
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

        MaintenanceTaskManager.setupTask(this,
                findViewById(R.id.check_filter), findViewById(R.id.date_filter), findViewById(R.id.layout_filter), 21, "filter cleaning");
        MaintenanceTaskManager.setupTask(this,
                findViewById(R.id.check_skimmer), findViewById(R.id.date_skimmer), findViewById(R.id.layout_skimmer), 30, "skimmer clearing");
        MaintenanceTaskManager.setupTask(this,
                findViewById(R.id.check_pump), findViewById(R.id.date_pump), findViewById(R.id.layout_pump), 30, "pump inspection");

        MaintenanceTaskManager.setupTestButtons(findViewById(R.id.btn_start_test), findViewById(R.id.btn_stop_test), this);
    }

    private String getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("userId", "user123");
    }

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            String currentUserId = getLoggedInUserId();
            fetchTemperature(currentUserId);
            fetchPh(currentUserId);
            fetchTds(currentUserId);
            fetchDepth(currentUserId);

            refreshHandler.postDelayed(this, 10 * 1000);
        }
    };

    private void setupUI() {
        temp_gauge = findViewById(R.id.temp_gauge);
        temp_gauge.setTitle("Temperature");
        temp_gauge.setMinValue(0f);
        temp_gauge.setMaxValue(50f);
        temp_gauge.setRanges(15f, 22f, 28f, 35f);

        depth_gauge = findViewById(R.id.depth_gauge);
        depth_gauge.setTitle("Depth");
        depth_gauge.setMinValue(0f);
        depth_gauge.setMaxValue(200f);
        depth_gauge.setRanges(150f, 160f, 185f, 195f);

        ph_gauge = findViewById(R.id.ph_gauge);
        ph_gauge.setTitle("Ph");
        ph_gauge.setMinValue(0f);
        ph_gauge.setMaxValue(14f);
        ph_gauge.setRanges(6.8f, 7.2f, 7.6f, 8.0f);

        tds_gauge = findViewById(R.id.tds_gauge);
        tds_gauge.setTitle("TDS");
        tds_gauge.setMinValue(0f);
        tds_gauge.setMaxValue(8000f);
        tds_gauge.setRanges(1500f, 2500f, 4500f, 6000f);
    }

    private void fetchTemperature(String userId) {
        temperatureController.fetchTemperatureForUser(userId, new TemperatureController.TemperatureCallback() {
            @Override
            public void onSuccess(TemperatureData temperatureData) {
                double temp = temperatureData.getTemperature();
                temp_gauge.setValueAnimated((float) temp);
            }
            @Override public void onEmpty() {}
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Temp: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPh(String userId) {
        phController.fetchPhForUser(userId, new PHController.PHCallback() {
            @Override
            public void onSuccess(PHData phData) {
                double ph = phData.getPh();
                ph_gauge.setValueAnimated((float) ph);
            }
            @Override public void onEmpty() {}
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "pH: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTds(String userId) {
        tdsController.fetchTdsForUser(userId, new TdsController.TdsCallback() {
            @Override
            public void onSuccess(TdsData tdsData) {
                double tds = tdsData.getTds();
                tds_gauge.setValueAnimated((float) tds);
            }
            @Override public void onEmpty() {}
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "TDS: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDepth(String userId) {
        depthController.fetchDepthForUser(userId, new DepthController.DepthCallback() {
            @Override
            public void onSuccess(DepthData depthData) {
                double depth = depthData.getDepth();
                depth_gauge.setValueAnimated((float) depth);
            }
            @Override public void onEmpty() {}
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Depth: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}