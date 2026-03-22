package com.example.myapplication.Model;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MaintenanceTaskManager {

    // This tracks if we are in "15 second" mode or "3 week" mode
    public static boolean isTestMode = false;

    public static void setupTask(Context context, CheckBox checkBox, TextView dateText, LinearLayout layout, int daysToAdd, String taskName) {

        dateText.setVisibility(View.GONE);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(context, "Task Done!", Toast.LENGTH_SHORT).show();
                layout.setBackgroundColor(Color.parseColor("#C8E6C9")); // Light Green

                if (isTestMode) {
                    // --- TEST MODE: 15 SECONDS ---
                    dateText.setText("Upcoming " + taskName + ": in 15 seconds");
                    dateText.setVisibility(View.VISIBLE);

                    // Timer to reset after 15 seconds
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isTestMode) { // Only reset if still in test mode
                            checkBox.setChecked(false);
                            Toast.makeText(context, taskName + " Reset!", Toast.LENGTH_SHORT).show();
                        }
                    }, 15000);

                } else {
                    // --- NORMAL MODE: 21-30 DAYS ---
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String futureDate = sdf.format(calendar.getTime());

                    dateText.setText("Upcoming " + taskName + ": " + futureDate);
                    dateText.setVisibility(daysToAdd <= 0 ? View.GONE : View.VISIBLE);
                }
            } else {
                // Back to Grey
                layout.setBackgroundColor(Color.parseColor("#F0F0F0"));
                dateText.setVisibility(View.GONE);
            }
        });
    }

    // This is the new method that handles the button clicks
    public static void setupTestButtons(Button startBtn, Button stopBtn, Context context) {
        startBtn.setOnClickListener(v -> {
            isTestMode = true;
            Toast.makeText(context, "Test Mode: ON (15s Reset)", Toast.LENGTH_LONG).show();
        });

        stopBtn.setOnClickListener(v -> {
            isTestMode = false;
            Toast.makeText(context, "Test Mode: OFF (Normal Schedule)", Toast.LENGTH_LONG).show();
        });
    }
}