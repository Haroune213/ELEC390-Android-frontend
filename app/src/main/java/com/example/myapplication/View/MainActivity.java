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
import com.example.myapplication.Model.TasksData;
import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Controller.DepthController;
import com.example.myapplication.Controller.PHController;
import com.example.myapplication.Controller.TdsController;
import com.example.myapplication.Controller.TemperatureController;
import com.example.myapplication.Model.DepthData;
import com.example.myapplication.Model.NotificationHelper;
import com.example.myapplication.Model.PHData;
import com.example.myapplication.Model.TasksRequest;
import com.example.myapplication.Model.TdsData;
import com.example.myapplication.Model.TemperatureData;
import com.example.myapplication.Model.UserPreferences;
import com.example.myapplication.R;
import com.example.myapplication.UI.GaugeView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.myapplication.Model.MaintenanceTaskManager;

import java.util.List;

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

        SharedPreferences checkboxesprefs = getSharedPreferences("GaugePrefs", MODE_PRIVATE);

        boolean showTemp = checkboxesprefs.getBoolean("showTemp", true);
        boolean showDepth = checkboxesprefs.getBoolean("showDepth", true);
        boolean showTds = checkboxesprefs.getBoolean("showTds", true);
        boolean showPh = checkboxesprefs.getBoolean("showPh", true);

        if (temp_gauge != null)
            temp_gauge.setVisibility(showTemp ? View.VISIBLE : View.GONE);

        if (depth_gauge != null)
            depth_gauge.setVisibility(showDepth ? View.VISIBLE : View.GONE);

        if (tds_gauge != null)
            tds_gauge.setVisibility(showTds ? View.VISIBLE : View.GONE);

        if (ph_gauge != null)
            ph_gauge.setVisibility(showPh ? View.VISIBLE : View.GONE);
        // ---------------------------
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Use the same preference name used in LoginActivity ("MyAppPrefs")
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        if (prefs.getBoolean("isLoggedIn", false)) {
            // This retrieves the numeric ID (e.g., "20") saved during login
            currentUserId = prefs.getString("userId", "");

            // Safety check: if currentUserId is empty, we shouldn't be here
            if (currentUserId.isEmpty()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // 2. Simplify getLoggedInUserId to ensure it returns the numeric string
    private String getLoggedInUserId() {
        // Return the class variable we verified in onStart
        return currentUserId != null ? currentUserId : "";
    }

    // 3. The refreshRunnable is already correct as it calls the updated getLoggedInUserId()
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // ALWAYS use "1" to pull data from the shared physical sensor
            String forceId = "1";
            fetchTemperature(forceId);
            fetchPh(forceId);
            fetchTds(forceId);
            fetchDepth(forceId);

            refreshHandler.postDelayed(this,  1000);
        }
    };

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

                    // Make background transparent so XML corners show correctly
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

        // Get the active user ID for the default maintenance tasks
        String userId = getLoggedInUserId();

        MaintenanceTaskManager.setupTask(this, userId, null,
                findViewById(R.id.check_filter), findViewById(R.id.date_filter), findViewById(R.id.layout_filter), 21, "filter cleaning");
        MaintenanceTaskManager.setupTask(this, userId, null,
                findViewById(R.id.check_skimmer), findViewById(R.id.date_skimmer), findViewById(R.id.layout_skimmer), 30, "skimmer clearing");
        MaintenanceTaskManager.setupTask(this, userId, null,
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

                // --- NEW UPDATED NAMES MATCHING XML ---
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
                        String description = taskInput.getText().toString().trim();
                        String freqStr = freqInput.getText().toString().trim();

                        if (!description.isEmpty() && !freqStr.isEmpty()) {
                            int inputVal = Integer.parseInt(freqStr);
                            int finalDays = 0;

                            // Convert selected unit to days
                            if (daysBtn.isChecked()) {
                                finalDays = inputVal;
                            } else if (weeksBtn.isChecked()) {
                                finalDays = inputVal * 7;
                            } else if (monthsBtn.isChecked()) {
                                finalDays = inputVal * 30;
                            }

                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            String userId = prefs.getString("userId", "");

                            if (userId.isEmpty()) {
                                Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            TasksRequest request = new TasksRequest(
                                    userId,
                                    description,
                                    finalDays,
                                    inputVal,
                                    false
                            );

                            ApiService api = ApiClient.getClient().create(ApiService.class);
                            api.postTask(request).enqueue(new Callback<TasksData>() {
                                @Override
                                public void onResponse(Call<TasksData> call, Response<TasksData> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        TasksData savedTask = response.body();

                                        LinearLayout container = findViewById(R.id.maintenance_container);
                                        MaintenanceTaskManager.createDynamicTask(
                                                MainActivity.this,
                                                userId,                  // Added
                                                savedTask.getId(),       // Added
                                                container,
                                                savedTask.getTaskDescription(),
                                                savedTask.getTimeSeparation()
                                        );
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to save task", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<TasksData> call, Throwable t) {
                                    Toast.makeText(MainActivity.this, "Backend error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

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
        api.getUserPreferences("1").enqueue(new Callback<UserPreferences>() {
            @Override
            public void onResponse(Call<UserPreferences> call, Response<UserPreferences> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyPreferencesToGauges(response.body());
                }
            }
            @Override
            public void onFailure(Call<UserPreferences> call, Throwable t) {}
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
        temperatureController.fetchTemperatureForUser(userId, new TemperatureController.TemperatureCallback() {
            @Override
            public void onSuccess(TemperatureData data) {
                temp_gauge.setValueAnimated(data.getTemperature().floatValue());
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
            public void onSuccess(PHData data) {
                ph_gauge.setValueAnimated((float)data.getPh());
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
            public void onSuccess(TdsData data) {
                tds_gauge.setValueAnimated((float) data.getTds());
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
            public void onSuccess(DepthData data) {
                depth_gauge.setValueAnimated((float) data.getDepth());
            }
            @Override public void onEmpty() {}
            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Depth: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadSavedTasks() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", "");

        if (userId.isEmpty()) {
            return;
        }

        LinearLayout container = findViewById(R.id.maintenance_container);
        container.removeAllViews();

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getTasksForUser(userId).enqueue(new Callback<List<TasksData>>() {
            @Override
            public void onResponse(Call<List<TasksData>> call, Response<List<TasksData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TasksData> tasks = response.body();

                    for (TasksData task : tasks) {
                        String desc = task.getTaskDescription();
                        int days = task.getTimeSeparation();

                        MaintenanceTaskManager.createDynamicTask(
                                MainActivity.this,
                                userId,         // Added
                                task.getId(),   // Added
                                container,
                                desc,
                                days
                        );
                    }
                }
            }
            @Override
            public void onFailure(Call<List<TasksData>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}