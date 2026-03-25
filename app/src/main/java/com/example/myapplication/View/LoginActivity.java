package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Controller.AuthentificationController;
import com.example.myapplication.R;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEdit, passwordEdit;
    private Button loginButton;
    private Button registerButton;
    private AuthentificationController authenticationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        usernameEdit = findViewById(R.id.usernameText);
        passwordEdit = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Initialize the Authentication Controller to handle API logic
        authenticationController = new AuthentificationController();

        // Redirect to RegisterActivity when the register button is clicked
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle Login button click
        loginButton.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            // Basic validation for empty fields
            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute the login request via the controller
            authenticationController.login(username, password, new AuthentificationController.AuthCallback() {
                @Override
                public void onSuccess(Map<String, String> data) {
                    // Retrieve authentication data sent by the Backend
                    String token = data.get("token");

                    // Priority: If the backend returns a specific "userId", use it.
                    // Otherwise, fall back to the "username" as the unique identifier for API requests.
                    String userId = data.containsKey("userId") ? data.get("userId") : username;

                    // Persist session data in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("isLoggedIn", true) // Mark user as authenticated
                            .putString("token", token)      // Store JWT token for future requests
                            .putString("userId", userId)    // CRITICAL: Used by MainActivity to fetch specific user data
                            .putString("username", username)
                            .apply();

                    // Navigate to the dashboard (MainActivity)
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    // Close LoginActivity so the user cannot navigate back to it via the back button
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    // Display error message from the server (e.g., "Invalid Credentials")
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}