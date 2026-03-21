package com.example.myapplication.Controller;

import com.example.myapplication.API.ApiClient;
import com.example.myapplication.API.ApiService;
import com.example.myapplication.Model.TdsData;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TdsController {

    private final ApiService apiService;
    public TdsController() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchTdsForUser(String userId, TdsController.TdsCallback callback) {
        apiService.getTdsForUser(userId).enqueue(new Callback<List<TdsData>>() {

            @Override
            public void onResponse(Call<List<TdsData>> call, Response<List<TdsData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TdsData> tds = response.body();

                    if (!tds.isEmpty()) {
                        TdsData latest = tds.get(tds.size() - 1);
                        callback.onSuccess(latest);
                    } else {
                        callback.onEmpty();
                    }
                } else {
                    callback.onError("Failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TdsData>> call, Throwable t) {
                callback.onError("Error: " + t.getMessage());
            }
        });
    }

    public interface TdsCallback {
        void onSuccess(TdsData tdsData);
        void onEmpty();
        void onError(String errorMessage);
    }
}
