package com.example.myapplication.Service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.example.myapplication.View.SettingsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() == null) return;

        // ── Vérifier si le type est togglé ON ────────────────────────────────
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Détecter le type depuis le titre (ex: "Pool Alert: temperature")
        String title = remoteMessage.getNotification().getTitle();
        String body  = remoteMessage.getNotification().getBody();

        boolean isAlert    = title != null && title.toLowerCase().contains("alert");
        boolean isReminder = title != null && title.toLowerCase().contains("reminder");

        boolean alertsEnabled    = prefs.getBoolean("Alert", true);
        boolean remindersEnabled = prefs.getBoolean("Reminder", true);

        // Bloquer si le type est désactivé
        if (isAlert && !alertsEnabled) return;
        if (isReminder && !remindersEnabled) return;
        // Si ni alert ni reminder dans le titre, on laisse passer par défaut

        showNotification(title, body);
    }

    private void showNotification(String title, String message) {
        String channelId = "sensor_alerts";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Sensor Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        // ── PendingIntent — redirige vers SettingsActivity au clic ───────────
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("openHistory", true); // signal pour scroller vers l'historique

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // ← redirection ajoutée

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this)
                    .notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}