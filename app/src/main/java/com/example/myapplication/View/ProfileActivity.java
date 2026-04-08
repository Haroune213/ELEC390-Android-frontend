package com.example.myapplication.View;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Controller.AuthentificationController;
import com.example.myapplication.Model.LoginRequest;
import com.example.myapplication.Model.PoolInfo;
import com.example.myapplication.Model.UserPreferences;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    protected Button logout_button, deleteAccount_button, edit_button, edit_profile_details_button, editPrefsButton;;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // User Info
        TextView usernameVal = findViewById(R.id.username_value);
        TextView emailVal = findViewById(R.id.email_value);

        // User Preferences
        TextView tempValue  = findViewById(R.id.textView22);
        TextView depthValue = findViewById(R.id.textView23);
        TextView tdsValue   = findViewById(R.id.textView26);
        TextView phValue    = findViewById(R.id.textView28);

        // Buttons
        logout_button = findViewById(R.id.logout_button);
        deleteAccount_button = findViewById(R.id.deleteAccount_button);
        edit_profile_details_button = findViewById(R.id.edit_button2);
        editPrefsButton = findViewById(R.id.edit_button3);

        // Save email+username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "N/A");
        String email = prefs.getString("email", "N/A"); // Ensure you save email during login/register too

        usernameVal.setText(username);
        emailVal.setText(email);

        // Charge preferences from API when app starts
        String userId = prefs.getString("userId",  "");
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getUserPreferences(userId).enqueue(new Callback<UserPreferences>() {
            @Override
            public void onResponse(Call<UserPreferences> call,
                                   Response<UserPreferences> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserPreferences p = response.body();
                    tempValue.setText(p.getTempMin() + " - " + p.getTempMax());
                    depthValue.setText(p.getDepthMin() + " - " + p.getDepthMax());
                    tdsValue.setText(p.getTdsMin() + " - " + p.getTdsMax());
                    phValue.setText(p.getPhMin() + " - " + p.getPhMax());
                }
            }
            @Override
            public void onFailure(Call<UserPreferences> call, Throwable t) {
                // Keep "Loading..." if no connection
            }
        });



        // Button initialization: User Info
        edit_profile_details_button.setOnClickListener(v -> {
            editProfileInfoDialogFragment dialog = new editProfileInfoDialogFragment();

            // Once dialog saves, updates TextViews directly
            dialog.setOnProfileUpdatedListener((newUsername, newEmail) -> {
                usernameVal.setText(newUsername);
                emailVal.setText(newEmail);
            });

            dialog.show(getSupportFragmentManager(), "editProfileInfo");
        });

        // Button initialization: User Preferences
        editPrefsButton.setOnClickListener(v -> {
            editPreferencesDialogFragment dialog = new editPreferencesDialogFragment();

            dialog.setOnPreferencesUpdatedListener((tempMin, tempMax, phMin, phMax,
                                                    tdsMin, tdsMax, depthMin, depthMax) -> {
                tempValue.setText(tempMin + " - " + tempMax);
                depthValue.setText(depthMin + " - " + depthMax);
                tdsValue.setText(tdsMin + " - " + tdsMax);
                phValue.setText(phMin + " - " + phMax);
            });

            dialog.show(getSupportFragmentManager(), "editPreferences");
        });

        // Button initialization: Logout
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear login status
                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                // 1. Call the backend to clear the FCM token
                AuthentificationController authController = new AuthentificationController();
                authController.logout(userId, new AuthentificationController.AuthCallback() {
                    @Override
                    public void onSuccess(Map<String, String> data) {
                        performLocalLogout(prefs);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Logout locally even if the network call fails to not trap the user
                        performLocalLogout(prefs);
                    }
                });

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            }
        });


        // Button initialization: Go to Dialog delete account
        deleteAccount_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProfileDialogFragment dialogFragment = new deleteProfileDialogFragment();

                dialogFragment.show(getSupportFragmentManager(), "deleteProfileDialogFragment");

            }
        });


        // Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setItemActiveIndicatorEnabled(false);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();  // get the ID of the selected item
            if (id == R.id.nav_profile) {
                // Already on MainActivity
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        // monitored data checkboxes behavior
        // Inside onCreate of ProfileActivity.java
        CheckBox cbTemp = findViewById(R.id.pH_checkBox4);
        CheckBox cbDepth = findViewById(R.id.pH_checkBox5);
        CheckBox cbTds = findViewById(R.id.pH_checkBox6);
        CheckBox cbPh = findViewById(R.id.pH_checkBox3);

        SharedPreferences checkboxesprefs = getSharedPreferences("GaugePrefs", MODE_PRIVATE);

// Set initial state from saved prefs (default to true)
        cbTemp.setChecked(checkboxesprefs.getBoolean("showTemp", true));
        cbDepth.setChecked(checkboxesprefs.getBoolean("showDepth", true));
        cbTds.setChecked(checkboxesprefs.getBoolean("showTds", true));
        cbPh.setChecked(checkboxesprefs.getBoolean("showPh", true));

// Save state when changed
        cbTemp.setOnCheckedChangeListener((v, isChecked) ->
                checkboxesprefs.edit().putBoolean("showTemp", isChecked).apply());

        cbDepth.setOnCheckedChangeListener((v, isChecked) ->
                checkboxesprefs.edit().putBoolean("showDepth", isChecked).apply());

        cbTds.setOnCheckedChangeListener((v, isChecked) ->
                checkboxesprefs.edit().putBoolean("showTds", isChecked).apply());

        cbPh.setOnCheckedChangeListener((v, isChecked) ->
                checkboxesprefs.edit().putBoolean("showPh", isChecked).apply());
    }
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    private void performLocalLogout(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.apply();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}