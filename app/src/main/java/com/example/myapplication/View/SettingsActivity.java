package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.Alert;
import com.example.myapplication.Model.LoginRequest;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    protected CheckBox alert_checkBox, reminder_checkBox;
    protected ListView notificationHistory_lv;
    protected SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // ── Initialisation ────────────────────────────────────────────────────
        alert_checkBox          = findViewById(R.id.alert_checkBox);
        reminder_checkBox       = findViewById(R.id.reminder_checkBox);
        notificationHistory_lv  = findViewById(R.id.notificationHistory_lv);
        TextView unreadBadge    = findViewById(R.id.unread_badge);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // ── Charger les toggles sauvegardés ───────────────────────────────────
        alert_checkBox.setChecked(sharedPreferences.getBoolean("Alert", true));
        reminder_checkBox.setChecked(sharedPreferences.getBoolean("Reminder", true));

        alert_checkBox.setOnCheckedChangeListener((btn, isChecked) ->
                sharedPreferences.edit().putBoolean("Alert", isChecked).apply());

        reminder_checkBox.setOnCheckedChangeListener((btn, isChecked) ->
                sharedPreferences.edit().putBoolean("Reminder", isChecked).apply());

        // ── Charger l'historique depuis l'API ─────────────────────────────────
        String userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("userId", "");

        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getAlertsForUser(userId).enqueue(new Callback<List<Alert>>() {
            @Override
            public void onResponse(Call<List<Alert>> call,
                                   Response<List<Alert>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Alert> alerts = response.body();

                    // Compter les non-résolus pour le badge
                    long unresolved = alerts.stream()
                            .filter(a -> a.getResolved() != null && !a.getResolved())
                            .count();
                    if (unresolved > 0) {
                        unreadBadge.setText(String.valueOf(unresolved));
                        unreadBadge.setVisibility(View.VISIBLE);
                    }

                    // Adapter personnalisé pour afficher chaque notification
                    ArrayAdapter<Alert> adapter = new ArrayAdapter<Alert>(
                            SettingsActivity.this,
                            R.layout.item_notification,
                            alerts) {
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView,
                                            @NonNull ViewGroup parent) {
                            if (convertView == null) {
                                convertView = LayoutInflater.from(getContext())
                                        .inflate(R.layout.item_notification, parent, false);
                            }
                            Alert alert = getItem(position);
                            if (alert == null) return convertView;

                            TextView titleTv    = convertView.findViewById(R.id.notif_title);
                            TextView messageTv  = convertView.findViewById(R.id.notif_message);
                            TextView timeTv     = convertView.findViewById(R.id.notif_time);
                            View     colorStrip = convertView.findViewById(R.id.color_strip);

                            titleTv.setText("Pool Alert: " + alert.getSensorType());
                            messageTv.setText(alert.getMessage());

                            // Formater le timestamp
                            try {
                                LocalDateTime dt = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    dt = LocalDateTime.parse(alert.getTimestamp(),
                                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    timeTv.setText(dt.format(
                                            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm")));
                                }
                            } catch (Exception e) {
                                timeTv.setText(alert.getTimestamp());
                            }

                            // Rouge si non-résolu, vert si résolu
                            boolean resolved = alert.getResolved() != null && alert.getResolved();
                            colorStrip.setBackgroundColor(resolved
                                    ? 0xFF76CCD7   // vert-bleu = résolu
                                    : 0xFFE46B4F); // orange-rouge = actif

                            return convertView;
                        }
                    };
                    notificationHistory_lv.setAdapter(adapter);

                    // Si on arrive depuis une notification, scroller vers l'historique
                    if (getIntent().getBooleanExtra("openHistory", false)) {
                        notificationHistory_lv.post(() ->
                                notificationHistory_lv.smoothScrollToPosition(0));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Alert>> call, Throwable t) {
                // Garder la liste vide si pas de connexion
            }
        });

        // ── Navigation ────────────────────────────────────────────────────────
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setItemActiveIndicatorEnabled(false);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) return true;
            else if (id == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

    }
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}