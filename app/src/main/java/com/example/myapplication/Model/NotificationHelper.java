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
    public static void createAlertChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alerts",
                    NotificationManager.IMPORTANCE_HIGH // <--- FORCE LE POP-UP
            );
            channel.setDescription("Critical pool alerts");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    public static void sendReminderNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Renamed the channel ID to force Android to register the new HIGH priority
        String reminderChannelId = "reminders_channel_high";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    reminderChannelId,
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH // FORCES THE HEADS-UP POP-UP
            );
            channel.setDescription("Routine maintenance tasks");
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // Create an action so clicking the notification opens the MainActivity
        android.content.Intent intent = new android.content.Intent(context, com.example.myapplication.View.MainActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_ONE_SHOT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, reminderChannelId)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // FORCES THE HEADS-UP POP-UP
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // Opens the app on click

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
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

