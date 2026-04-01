package com.example.myapplication.View;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Controller.AuthentificationController;
// Importations nécessaires en haut du fichier
import com.google.firebase.messaging.FirebaseMessaging;
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

        loginButton.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Demander le Token à Firebase
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        String fcmToken = "";
                        if (task.isSuccessful() && task.getResult() != null) {
                            fcmToken = task.getResult(); // On récupère l'adresse du téléphone
                        }

                        // 2. Envoyer le login avec le jeton récupéré
                        authenticationController.login(username, password, fcmToken, new AuthentificationController.AuthCallback() {
                            @Override
                            public void onSuccess(Map<String, String> data) {
                                // Ta logique de succès existante (SharedPreferences, etc.)
                                String token = data.get("token");
                                String userId = data.containsKey("userId") ? data.get("userId") : username;

                                SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                prefs.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("token", token)
                                        .putString("userId", userId)
                                        .putString("username", username)
                                        .apply();

                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }
}