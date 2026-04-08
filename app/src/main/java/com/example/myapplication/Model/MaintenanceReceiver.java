package com.example.myapplication.Model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MaintenanceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("Reminder", true)) {
            return;
        }

        // Verify the timer belongs to the currently logged-in user
        String taskUserId = intent.getStringExtra("userId");
        SharedPreferences authPrefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String currentUserId = authPrefs.getString("userId", "");

        if (taskUserId != null && !taskUserId.equals(currentUserId)) {
            return; // User has changed, ignore the alarm
        }

        String taskName = intent.getStringExtra("taskName");
        if (taskName != null) {
            NotificationHelper.sendReminderNotification(
                    context,
                    "Pool Reminder",
                    taskName + " is ready to be done."
            );
        }
    }
}