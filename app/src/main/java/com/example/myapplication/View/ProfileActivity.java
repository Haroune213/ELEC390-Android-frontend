package com.example.myapplication.View;

import android.annotation.SuppressLint;
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

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.LoginRequest;
import com.example.myapplication.Model.PoolInfo;
import com.example.myapplication.Model.UserPreferences;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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

        // Pool Info
        TextView typeValue      = findViewById(R.id.type_value);
        TextView dimensionValue = findViewById(R.id.dimension_value);
        TextView unitsValue     = findViewById(R.id.units_value);

        // Buttons
        logout_button = findViewById(R.id.logout_button);
        deleteAccount_button = findViewById(R.id.deleteAccount_button);
        edit_button = findViewById(R.id.edit_button);
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

        // Charge pool info from API when app starts
        api.getPoolInfo(userId).enqueue(new Callback<PoolInfo>() {
            @Override
            public void onResponse(Call<PoolInfo> call, Response<PoolInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PoolInfo p = response.body();
                    typeValue.setText(p.getPoolType());
                    dimensionValue.setText(p.getWidth() + " x " + p.getLength()
                            + " x " + p.getDepth());
                    unitsValue.setText(p.getUnit());
                }
            }
            @Override
            public void onFailure(Call<PoolInfo> call, Throwable t) {}
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

        // Button initialization: Go to Dialog edit pool information, callback after save info
        edit_button.setOnClickListener(v -> {
            editPoolInfoDialogFragment dialog = new editPoolInfoDialogFragment();
            dialog.setOnPoolInfoUpdatedListener((type, width, depth, length, unit) -> {
                typeValue.setText(type);
                dimensionValue.setText(width + " x " + length + " x " + depth);
                unitsValue.setText(unit);
            });
            dialog.show(getSupportFragmentManager(), "editPoolInfoDialogFragment");
        });

        // Navigation
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