package com.example.myapplication.Model;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.myapplication.R;


public class NotificationHelper {

    private static final String CHANNEL_ID = "alerts_channel";
    public static void sendNotification(Context context, String title, String message) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Alerts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alerts", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)    // Find new icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Permission for POST required for this function
        manager.notify((int) System.currentTimeMillis(), builder.build());

        // Create channel for Reminder. default priority.

    }

    public static void saveNotification(Context context, String title, String message) {

        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        String history = prefs.getString("history", "");
        history = title + ": " + message + "\n" + history;

        prefs.edit().putString("history", history).apply();
    }
}

