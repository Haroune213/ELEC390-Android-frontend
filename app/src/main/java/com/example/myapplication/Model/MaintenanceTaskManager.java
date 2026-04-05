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

    @SuppressLint("SetTextI18n")
    public static void setupTask(Context context, CheckBox checkBox, TextView dateText, LinearLayout layout, int daysToAdd, String taskName) {

        dateText.setVisibility(View.GONE);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(context, "Task Done!", Toast.LENGTH_SHORT).show();
                layout.setBackgroundColor(Color.parseColor("#C8E6C9")); // Green for everyone when done

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String futureDate = sdf.format(calendar.getTime());

                dateText.setText("Upcoming " + taskName + ": " + futureDate);
                dateText.setVisibility(daysToAdd <= 0 ? View.GONE : View.VISIBLE);

            } else {
                // Return to original colors when unchecked
                // We check if it's one of your original predefined tasks
                String nameLower = taskName.toLowerCase();
                if (nameLower.contains("filter") || nameLower.contains("skimmer") || nameLower.contains("pump")) {
                    layout.setBackgroundColor(Color.parseColor("#F0F0F0")); // Original Grey/Green
                } else {
                    layout.setBackgroundColor(Color.parseColor("#D1F2FF")); // User-created Light Blue
                }
                dateText.setVisibility(View.GONE);
            }
        });

        // Auto-reset logic
        if (daysToAdd > 0) {
            long delayMillis = (long) daysToAdd * 24 * 60 * 60 * 1000;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                checkBox.setChecked(false);
            }, delayMillis);
        }
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