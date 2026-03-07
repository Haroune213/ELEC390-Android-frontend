package com.example.myapplication.Model;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
public interface ApiService {
    @POST("/auth/login")
    Call<Map<String, String>> login(@Body LoginRequest loginRequest);

    @POST("/auth/register")
    Call<String> register(@Body LoginRequest registerRequest);

    @POST("/auth/register")
    Call<Map<String, String>> register(@Body RegisterRequest registerRequest);

    @GET("/api/temperature/{userId}")
    Call<List<TemperatureData>> getTemperaturesForUser(@Path("userId") String userId);
}