package com.example.myapplication.Model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MaintenanceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the user toggled Reminders OFF in SettingsActivity
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("Reminder", true)) {
            return; // Do nothing if notifications are disabled
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