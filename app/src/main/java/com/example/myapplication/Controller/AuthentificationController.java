package com.example.myapplication.Controller;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.LoginRequest;
import com.example.myapplication.Model.RegisterRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthentificationController {

    private final ApiService apiService;

    public AuthentificationController() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    // Ajoute le paramètre String fcmToken ici
    public void login(String username, String password, String fcmToken, AuthCallback callback) {
        // Utilise le nouveau constructeur à 3 paramètres défini dans ton modèle LoginRequest
        LoginRequest request = new LoginRequest(username, password, fcmToken);

        apiService.login(request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Login failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public void register(String username, String email, String password, RegisterCallback callback) {
        RegisterRequest request = new RegisterRequest(username, email, password);

        apiService.register(request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Register failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }
    public void logout(String userId, AuthCallback callback) {
        apiService.logout(userId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Logout failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public interface AuthCallback {
        void onSuccess(Map<String, String> data);
        void onError(String errorMessage);
    }

    public interface RegisterCallback {
        void onSuccess(Map<String, String> data);
        void onError(String errorMessage);
    }
}