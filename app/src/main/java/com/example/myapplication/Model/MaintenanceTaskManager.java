package com.example.myapplication.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.content.SharedPreferences;

public class MaintenanceTaskManager {

    // Replace your entire setupTask method with this:
    @SuppressLint("SetTextI18n")
    public static void setupTask(Context context, CheckBox checkBox, TextView dateText, LinearLayout layout, int daysToAdd, String taskName) {

        android.content.SharedPreferences prefs = context.getSharedPreferences("TaskStates", Context.MODE_PRIVATE);
        long savedTargetTime = prefs.getLong(taskName + "_target", 0);

        // Restore visual state if a timer is currently active
        if (savedTargetTime > System.currentTimeMillis()) {
            checkBox.setChecked(true);
            layout.setBackgroundColor(Color.parseColor("#C8E6C9"));
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateText.setText("Upcoming " + taskName + ": " + sdf.format(new java.util.Date(savedTargetTime)));
            dateText.setVisibility(View.VISIBLE);
        } else {
            dateText.setVisibility(View.GONE);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            android.content.Intent intent = new android.content.Intent(context, MaintenanceReceiver.class);
            intent.putExtra("taskName", taskName);

            int requestCode = taskName.hashCode();
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                    context, requestCode, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            if (isChecked) {
                Toast.makeText(context, "Task Done! Timer started.", Toast.LENGTH_SHORT).show();
                layout.setBackgroundColor(Color.parseColor("#C8E6C9"));

                long delayMillis = (long)  1000;
                long targetTime = System.currentTimeMillis() + delayMillis;

                // Save the target time so it survives app restarts
                prefs.edit().putLong(taskName + "_target", targetTime).apply();

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                dateText.setText("Upcoming " + taskName + ": " + sdf.format(new java.util.Date(targetTime)));
                dateText.setVisibility(View.VISIBLE);

                if (daysToAdd > 0 && alarmManager != null) {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, targetTime, pendingIntent);
                }

            } else {
                String nameLower = taskName.toLowerCase();
                if (nameLower.contains("filter") || nameLower.contains("skimmer") || nameLower.contains("pump")) {
                    layout.setBackgroundColor(Color.parseColor("#F0F0F0"));
                } else {
                    layout.setBackgroundColor(Color.parseColor("#D1F2FF"));
                }
                dateText.setVisibility(View.GONE);

                // Clear the saved state so it does not load next time
                prefs.edit().remove(taskName + "_target").apply();

                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
            }
        });
    }

    public static void createDynamicTask(Context context, LinearLayout container, String taskDescription, int frequencyDays) {
        LinearLayout taskLayout = new LinearLayout(context);

        // 1. Spacing Fix: Added Top Margin (20) to separate from predefined tasks
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 20);
        taskLayout.setLayoutParams(params);

        taskLayout.setOrientation(LinearLayout.HORIZONTAL);

        // 2. Color Fix: Set initial color to Light Blue
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

        // --- LONG-PRESS DELETE ---
        taskLayout.setOnLongClickListener(v -> {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete '" + taskDescription + "'?");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                // 1. Remove from UI
                container.removeView(taskLayout);

                // 2. Remove from SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences("DynamicTasks", Context.MODE_PRIVATE);
                String taskList = prefs.getString("taskList", "");
                String taskToRemove = taskDescription + "|" + frequencyDays + ";";

                // Replace the specific task string with nothing
                String updatedList = taskList.replace(taskToRemove, "");
                prefs.edit().putString("taskList", updatedList).apply();

                Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        });

        container.addView(taskLayout);

        // 3. Connect logic
        setupTask(context, checkBox, dateText, taskLayout, frequencyDays, taskDescription);
    }

    public static void saveTask(Context context, String description, int days) {
        SharedPreferences prefs = context.getSharedPreferences("DynamicTasks", Context.MODE_PRIVATE);
        String existingTasks = prefs.getString("taskList", "");
        // Store as "Description|Days;Description|Days;"
        String newTask = description + "|" + days + ";";
        prefs.edit().putString("taskList", existingTasks + newTask).apply();
    }

}