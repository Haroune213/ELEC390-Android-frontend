package com.example.myapplication.View;

import android.app.Dialog;
import android.widget.Button;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Controller.DepthController;
import com.example.myapplication.Controller.PHController;
import com.example.myapplication.Controller.TdsController;
import com.example.myapplication.Controller.TemperatureController;
import com.example.myapplication.Model.DepthData;
import com.example.myapplication.Model.NotificationHelper;
import com.example.myapplication.Model.PHData;
import com.example.myapplication.Model.TdsData;
import com.example.myapplication.Model.TemperatureData;
import com.example.myapplication.Model.UserPreferences;
import com.example.myapplication.R;
import com.example.myapplication.UI.GaugeView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.Model.MaintenanceTaskManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        overridePendingTransition(0, 0);
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupUI();

        ImageView infoBtn = findViewById(R.id.info); // Ensure this ID matches your info icon in activity_main.xml

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Create the dialog using the dialog_info_box XML
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_info_box);

                // 2. Set the custom dimensions (360dp x 480dp)
                if (dialog.getWindow() != null) {
                    int widthInPx = (int) (375 * getResources().getDisplayMetrics().density);
                    int heightInPx = (int) (500 * getResources().getDisplayMetrics().density);
                    dialog.getWindow().setLayout(widthInPx, heightInPx);

                    // Make background transparent so your XML corners show correctly
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                // 3. Set up the Arrow vector to close the dialog
                ImageView arrowClose = dialog.findViewById(R.id.arrow);
                arrowClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss(); // Closes the popup
                    }
                });

                dialog.show();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setItemActiveIndicatorEnabled(false);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
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


        ImageView addButton = findViewById(R.id.add);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_new_task);

                if (dialog.getWindow() != null) {
                    int widthInPx = (int) (375 * getResources().getDisplayMetrics().density);
                    int heightInPx = (int) (500 * getResources().getDisplayMetrics().density);
                    dialog.getWindow().setLayout(widthInPx, heightInPx);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }

                // --- NEW UPDATED NAMES MATCHING YOUR XML ---
                Button cancelBtn = dialog.findViewById(R.id.cancelTask_btn);
                Button addTaskBtn = dialog.findViewById(R.id.addTask_btn);
                EditText taskInput = dialog.findViewById(R.id.taskName_txt);
                EditText freqInput = dialog.findViewById(R.id.editTextText);

                RadioButton daysBtn = dialog.findViewById(R.id.days_radiobtn);
                RadioButton weeksBtn = dialog.findViewById(R.id.weeks_radiobtn);
                RadioButton monthsBtn = dialog.findViewById(R.id.months_radiobtn);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                addTaskBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String description = taskInput.getText().toString();
                        String freqStr = freqInput.getText().toString();

                        if (!description.isEmpty() && !freqStr.isEmpty()) {
                            int inputVal = Integer.parseInt(freqStr);
                            int finalDays = 0;

                            // Logic to handle Day(s), Week(s), or Month(s)
                            if (daysBtn.isChecked()) {
                                finalDays = inputVal;
                            } else if (weeksBtn.isChecked()) {
                                finalDays = inputVal * 7;
                            } else if (monthsBtn.isChecked()) {
                                finalDays = inputVal * 30;
                            }

                            LinearLayout container = findViewById(R.id.maintenance_container);
                            MaintenanceTaskManager.createDynamicTask(MainActivity.this, container, description, finalDays);

                            MaintenanceTaskManager.saveTask(MainActivity.this, description, finalDays);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show();
            }
        });

        loadSavedTasks();
    }

    private String getLoggedInUserId() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return prefs.getString("userId", "");
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
        temp_gauge  = findViewById(R.id.temp_gauge);
        depth_gauge = findViewById(R.id.depth_gauge);
        ph_gauge    = findViewById(R.id.ph_gauge);
        tds_gauge   = findViewById(R.id.tds_gauge);

        // Gauge Titles
        temp_gauge.setTitle("Temperature (C)");
        depth_gauge.setTitle("Depth (Cm)");
        ph_gauge.setTitle("pH");
        tds_gauge.setTitle("TDS (PPM)");

        // Ranges by default

        // Fetch preferences to update ranges
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getUserPreferences(userId).enqueue(new Callback<UserPreferences>() {
            @Override
            public void onResponse(Call<UserPreferences> call,
                                   Response<UserPreferences> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserPreferences p = response.body();
                    applyPreferencesToGauges(p);
                }
            }
            @Override
            public void onFailure(Call<UserPreferences> call, Throwable t) {
                // Keep default ranges if fail
            }
        });
    }


    private void applyPreferencesToGauges(UserPreferences p) {
        if (p.getTempMin() != null && p.getTempMax() != null) {
            temp_gauge.setMinValue(0f);
            temp_gauge.setMaxValue(40f);
            // low danger | low ok | high ok | high danger
            temp_gauge.setRanges(
                    p.getTempMin().floatValue() - 5f,
                    p.getTempMin().floatValue(),
                    p.getTempMax().floatValue(),
                    p.getTempMax().floatValue() + 5f
            );
        }
        if (p.getPhMin() != null && p.getPhMax() != null) {
            ph_gauge.setMinValue(0f);
            ph_gauge.setMaxValue(14f);
            ph_gauge.setRanges(
                    p.getPhMin().floatValue() - 0.5f,
                    p.getPhMin().floatValue(),
                    p.getPhMax().floatValue(),
                    p.getPhMax().floatValue() + 0.5f
            );
        }
        if (p.getTdsMin() != null && p.getTdsMax() != null) {
            tds_gauge.setMinValue(0f);
            tds_gauge.setMaxValue(6000f);
            tds_gauge.setRanges(
                    p.getTdsMin().floatValue() - 300f,
                    p.getTdsMin().floatValue(),
                    p.getTdsMax().floatValue(),
                    p.getTdsMax().floatValue() + 300f
            );
        }
        if (p.getDepthMin() != null && p.getDepthMax() != null) {
            depth_gauge.setMinValue(0f);
            depth_gauge.setMaxValue(500f);
            depth_gauge.setRanges(
                    p.getDepthMin().floatValue() - 10f,
                    p.getDepthMin().floatValue(),
                    p.getDepthMax().floatValue(),
                    p.getDepthMax().floatValue() + 10f
            );
        }
    }

    private void fetchTemperature(String userId) {
        temperatureController.fetchTemperatureForUser("1", new TemperatureController.TemperatureCallback() {
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
        phController.fetchPhForUser("1", new PHController.PHCallback() {
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
        tdsController.fetchTdsForUser("1", new TdsController.TdsCallback() {
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
        depthController.fetchDepthForUser("1", new DepthController.DepthCallback() {
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
    private void loadSavedTasks() {
        SharedPreferences prefs = getSharedPreferences("DynamicTasks", MODE_PRIVATE);
        String taskList = prefs.getString("taskList", "");

        if (!taskList.isEmpty()) {
            LinearLayout container = findViewById(R.id.maintenance_container);
            // Clear dynamic views before reloading to prevent duplicates
            // (Optional: only if you call this multiple times)

            String[] tasks = taskList.split(";");
            for (String taskData : tasks) {
                // Trim and check if the string is valid to prevent crashes on empty segments
                if (taskData.trim().contains("|")) {
                    String[] parts = taskData.split("\\|");
                    if (parts.length == 2) {
                        String desc = parts[0];
                        int days = Integer.parseInt(parts[1]);
                        MaintenanceTaskManager.createDynamicTask(this, container, desc, days);
                    }
                }
            }
        }
    }
}