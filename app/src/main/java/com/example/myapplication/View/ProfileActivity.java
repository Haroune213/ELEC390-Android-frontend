package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    protected Button logout_button, deleteAccount_button, edit_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logout_button = findViewById(R.id.logout_button);
        deleteAccount_button = findViewById(R.id.deleteAccount_button);
        edit_button = findViewById(R.id.edit_button);

        // Logout
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear login status
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });

        // Go to Dialog delete account
        deleteAccount_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProfileDialogFragment dialogFragment = new deleteProfileDialogFragment();

                dialogFragment.show(getSupportFragmentManager(), "deleteProfileDialogFragment");

            }
        });

        // Go to Dialog edit pool information
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPoolInfoDialogFragment dialogFragment = new editPoolInfoDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "editPoolInfoDialogFragment");

            }
        });

        //Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();  // get the ID of the selected item
            if (id == R.id.nav_profile) {
                // Already on MainActivity
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}