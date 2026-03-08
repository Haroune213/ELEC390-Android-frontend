package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Controller.AuthentificationController;
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

        usernameEdit = findViewById(R.id.usernameText);
        passwordEdit = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);


        // Initialize Controller
        authenticationController = new AuthentificationController();

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // CALL CONTROLLER
            authenticationController.login(username, password, new AuthentificationController.AuthCallback() {
                @Override
                public void onSuccess(Map<String, String> data) {
                    // Save token / login state
                    String token = data.get("token");
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("isLoggedIn", true)
                            .putString("token", token)
                            .putString("username", username)
                            .apply();

                    // Move to MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}