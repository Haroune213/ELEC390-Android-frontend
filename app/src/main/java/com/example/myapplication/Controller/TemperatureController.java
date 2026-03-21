package com.example.myapplication.Controller;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.TemperatureData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TemperatureController {

    private final ApiService apiService;
    public TemperatureController() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchTemperatureForUser(String userId, TemperatureCallback callback) {
        apiService.getTemperaturesForUser(userId).enqueue(new Callback<List<TemperatureData>>() {

            @Override
            public void onResponse(Call<List<TemperatureData>> call, Response<List<TemperatureData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TemperatureData> temperatures = response.body();

                    if (!temperatures.isEmpty()) {
                        TemperatureData latest = temperatures.get(temperatures.size() - 1);
                        callback.onSuccess(latest);
                    } else {
                        callback.onEmpty();
                    }
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TemperatureData>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public interface TemperatureCallback {
        void onSuccess(TemperatureData temperatureData);
        void onEmpty();
        void onError(String errorMessage);
    }
}