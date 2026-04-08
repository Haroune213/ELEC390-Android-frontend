package com.example.myapplication.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceTaskManager {

    private static void updateTaskStatusInBackend(Long taskId, boolean isCompleted) {
        if (taskId == null) return; // Skip for hardcoded default tasks

        ApiService api = ApiClient.getClient().create(ApiService.class);
        Map<String, Boolean> body = new HashMap<>();
        body.put("completed", isCompleted);

        api.updateTaskStatus(taskId, body).enqueue(new Callback<TasksData>() {
            @Override
            public void onResponse(Call<TasksData> call, Response<TasksData> response) {}
            @Override
            public void onFailure(Call<TasksData> call, Throwable t) {}
        });
    }

    @SuppressLint("SetTextI18n")
    // ADDED taskId PARAMETER
    public static void setupTask(Context context, String userId, Long taskId, CheckBox checkBox, TextView dateText, LinearLayout layout, int daysToAdd, String taskName) {

        SharedPreferences prefs = context.getSharedPreferences("TaskStates", Context.MODE_PRIVATE);
        String prefKey = userId + "_" + taskName + "_target";
        long savedTargetTime = prefs.getLong(prefKey, 0);

        // 1. Restore visual state on startup
        if (savedTargetTime > System.currentTimeMillis()) {
            checkBox.setChecked(true);
            layout.setBackgroundColor(Color.parseColor("#C8E6C9"));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            dateText.setText("Upcoming " + taskName + ": " + sdf.format(new java.util.Date(savedTargetTime)));
            dateText.setVisibility(View.VISIBLE);

            long timeLeft = savedTargetTime - System.currentTimeMillis();
            checkBox.postDelayed(() -> checkBox.setChecked(false), timeLeft);

        } else {
            // Timer expired while the app was closed
            dateText.setVisibility(View.GONE);
            if (savedTargetTime != 0) {
                prefs.edit().remove(prefKey).apply();
                updateTaskStatusInBackend(taskId, false); // <--- SYNC: Revert to false because timer expired!
            }
        }

        // 2. Listen for User Clicks
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            android.content.Intent intent = new android.content.Intent(context, MaintenanceReceiver.class);
            intent.putExtra("taskName", taskName);
            intent.putExtra("userId", userId);

            int requestCode = (userId + taskName).hashCode();
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            if (isChecked) {
                Toast.makeText(context, "Task Done! Timer started.", Toast.LENGTH_SHORT).show();
                layout.setBackgroundColor(Color.parseColor("#C8E6C9"));

                updateTaskStatusInBackend(taskId, true); // <--- SYNC: Mark as TRUE in database

                // To test quickly, change this back to 10000 (10 seconds)
                long delayMillis = /*(long) daysToAdd * 24 * 60 * 60 * 1000*/ 15000;
                long targetTime = System.currentTimeMillis() + delayMillis;

                prefs.edit().putLong(prefKey, targetTime).apply();

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                dateText.setText("Upcoming " + taskName + ": " + sdf.format(new java.util.Date(targetTime)));
                dateText.setVisibility(View.VISIBLE);

                if (daysToAdd > 0 && alarmManager != null) {
                    alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, targetTime, pendingIntent);
                    checkBox.postDelayed(() -> checkBox.setChecked(false), delayMillis);
                }

            } else {
                String nameLower = taskName.toLowerCase();
                if (nameLower.contains("filter") || nameLower.contains("skimmer") || nameLower.contains("pump")) {
                    layout.setBackgroundColor(Color.parseColor("#F0F0F0"));
                } else {
                    layout.setBackgroundColor(Color.parseColor("#D1F2FF"));
                }
                dateText.setVisibility(View.GONE);

                prefs.edit().remove(prefKey).apply();
                updateTaskStatusInBackend(taskId, false); // <--- SYNC: Revert to false (unchecked manually or timer finished while app open)

                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
            }
        });
    }

    // ADDED userId AND taskId PARAMETERS
    public static void createDynamicTask(Context context, String userId, Long taskId, LinearLayout container, String taskDescription, int frequencyDays) {
        LinearLayout taskLayout = new LinearLayout(context);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 20);
        taskLayout.setLayoutParams(params);
        taskLayout.setOrientation(LinearLayout.HORIZONTAL);
        taskLayout.setBackgroundColor(Color.parseColor("#D1F2FF"));
        taskLayout.setPadding(30, 30, 30, 30);
        taskLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        textLayout.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText(taskDescription);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.BLACK);

        TextView dateText = new TextView(context);
        dateText.setTextSize(12);
        dateText.setVisibility(View.GONE);

        textLayout.addView(title);
        textLayout.addView(dateText);

        CheckBox checkBox = new CheckBox(context);

        taskLayout.addView(textLayout);
        taskLayout.addView(checkBox);

        // --- BACKEND DELETE LOGIC ---
        taskLayout.setOnLongClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete '" + taskDescription + "'?");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Call backend API to delete the task permanently
                ApiService api = ApiClient.getClient().create(ApiService.class);
                api.deleteTask(userId, taskId).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            container.removeView(taskLayout);

                            // Also clear the local timer if it was running
                            SharedPreferences prefs = context.getSharedPreferences("TaskStates", Context.MODE_PRIVATE);
                            prefs.edit().remove(userId + "_" + taskDescription + "_target").apply();

                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        // The backend might return pure text which Retrofit struggles to parse as JSON, but the delete still worked
                        container.removeView(taskLayout);
                    }
                });
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        });

        container.addView(taskLayout);

        // Pass the taskId to setupTask
        setupTask(context, userId, taskId, checkBox, dateText, taskLayout, frequencyDays, taskDescription);
    }
}