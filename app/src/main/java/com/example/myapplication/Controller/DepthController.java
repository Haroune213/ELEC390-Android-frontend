package com.example.myapplication.Controller;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.DepthData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DepthController {

    private final ApiService apiService;
    public DepthController() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchDepthForUser(String userId, DepthController.DepthCallback callback) {
        apiService.getDepthForUser(userId).enqueue(new Callback<List<DepthData>>() {
            @Override
            public void onResponse(Call<List<DepthData>> call, Response<List<DepthData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DepthData> depth = response.body();

                    if (!depth.isEmpty()) {
                        DepthData latest = depth.get(depth.size() - 1);
                        callback.onSuccess(latest);
                    } else {
                        callback.onEmpty();
                    }
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<DepthData>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public interface DepthCallback {
        void onSuccess(DepthData depthData);
        void onEmpty();
        void onError(String errorMessage);
    }
}
