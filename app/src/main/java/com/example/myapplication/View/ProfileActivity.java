package com.example.myapplication.View;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    protected Button logout_button, deleteAccount_button, edit_button, edit_profile_details_button;;

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

        // Inside onCreate
        TextView usernameVal = findViewById(R.id.username_value);
        TextView emailVal = findViewById(R.id.email_value);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "N/A");
        String email = prefs.getString("email", "N/A"); // Ensure you save email during login/register too

        usernameVal.setText(username);
        emailVal.setText(email);

        // Initialize the button for User Info (the one next to email/username)
        edit_profile_details_button = findViewById(R.id.edit_button2);

        // Logic for the popup
        edit_profile_details_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Create the dialog using your XML layout
                final Dialog dialog = new Dialog(ProfileActivity.this);
                dialog.setContentView(R.layout.dialog_edit_profile_info);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                }
                dialog.show();

                // 2. Set up the Cancel button inside the popup
                Button cancel_button = dialog.findViewById(R.id.cancel_button);
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss(); // This closes the popup
                    }
                });

                // Note: The Save button is there but won't do anything for now
                // since we aren't touching the backend or storage yet.
            }
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