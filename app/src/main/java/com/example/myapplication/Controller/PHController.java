package com.example.myapplication.Controller;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.PHData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PHController {

    private final ApiService apiService;
    public PHController() { apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchPhForUser(String userId, PHController.PHCallback callback) {
        apiService.getPhForUser(userId).enqueue(new Callback<List<PHData>>() {
            @Override
            public void onResponse(Call<List<PHData>> call, Response<List<PHData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PHData> ph = response.body();

                    if (!ph.isEmpty()) {
                        PHData latest = ph.get(ph.size() - 1);
                        callback.onSuccess(latest);
                    } else {
                        callback.onEmpty();
                    }
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PHData>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public interface PHCallback {
        void onSuccess(PHData phData);
        void onEmpty();
        void onError(String errorMessage);
    }
}
