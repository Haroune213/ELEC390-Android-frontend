package com.example.myapplication.Controller;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.pHData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class pHController {

    private final ApiService apiService;
    public pHController() { apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchPhForUser(String userId, pHController.pHCallback callback) {
        apiService.getPhForUser(userId).enqueue(new Callback<List<pHData>>() {
            @Override
            public void onResponse(Call<List<pHData>> call, Response<List<pHData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<pHData> ph = response.body();

                    if (!ph.isEmpty()) {
                        pHData latest = ph.get(ph.size() - 1);
                        callback.onSuccess(latest);
                    } else {
                        callback.onEmpty();
                    }
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<pHData>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public interface pHCallback {
        void onSuccess(pHData phData);
        void onEmpty();
        void onError(String errorMessage);
    }
}
