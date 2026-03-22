package com.example.myapplication.Model;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
public class MaintenanceTaskManager {
    public static void setupTask(Context context, CheckBox checkBox, TextView dateText, LinearLayout layout, int daysToAdd, String taskName) {

        dateText.setVisibility(View.GONE);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 1. Toast & Green Color
                Toast.makeText(context, "Task Done!", Toast.LENGTH_SHORT).show();
                layout.setBackgroundColor(Color.parseColor("#C8E6C9")); // Light Green

                // 2. Calculate Future Date
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String futureDate = sdf.format(calendar.getTime());

                // 3. Set text
                dateText.setText("Upcoming " + taskName + ": " + futureDate);

                // 4. Logic: Disappear if date is today (for testing, this shows if daysToAdd > 0)
                if (daysToAdd <= 0) {
                    dateText.setVisibility(View.GONE);
                } else {
                    dateText.setVisibility(View.VISIBLE);
                }
            } else {
                // 5. Undo: Back to Grey and hide date
                layout.setBackgroundColor(Color.parseColor("#F0F0F0"));
                dateText.setVisibility(View.GONE);
            }
        });
    }
}