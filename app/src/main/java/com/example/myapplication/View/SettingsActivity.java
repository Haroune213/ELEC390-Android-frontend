package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    protected CheckBox alert_checkBox, reminder_checkBox;
    protected RadioGroup frequency_radioGroup;
    protected ListView notificationHistory_lv;
    protected SharedPreferences sharedPreferences;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            alert_checkBox = findViewById(R.id.alert_checkBox);
            reminder_checkBox = findViewById(R.id.alert_checkBox);
            frequency_radioGroup = findViewById(R.id.frequency_radioGroup);

            sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

            // Load saved values
            alert_checkBox.setChecked(sharedPreferences.getBoolean("Alert", true));
            reminder_checkBox.setChecked(sharedPreferences.getBoolean("Reminder", true));

            // Save values when box is checked
            alert_checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
                sharedPreferences.edit().putBoolean("Alert", isChecked).apply();
            });

            reminder_checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
                sharedPreferences.edit().putBoolean("Reminder", isChecked).apply();
            });

            // Save Notification frequency settings
            frequency_radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                sharedPreferences.edit().putInt("Frequency", checkedId).apply();
            });

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation Bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}