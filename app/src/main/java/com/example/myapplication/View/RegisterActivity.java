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

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;

    private Button registerButton;
    private Button loginButton;

    private AuthentificationController authenticationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEdit2);
        emailEditText = findViewById(R.id.emailEdit);
        passwordEditText = findViewById(R.id.passwordEdit2);
        registerButton = findViewById(R.id.registerButton2);
        loginButton = findViewById(R.id.loginButton2);

        authenticationController = new AuthentificationController();

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticationController.register(username, email, password,
                    new AuthentificationController.RegisterCallback() {
                        @Override
                        public void onSuccess(Map<String, String> data) {

                            String token = data.get("token");
                            String message = data.get("message");

                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString("username", username)
                                    .putString("email", email)
                                    .putString("token", token)
                                    .apply();

                            Toast.makeText(RegisterActivity.this,
                                    message != null ? message : "Registration successful",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}