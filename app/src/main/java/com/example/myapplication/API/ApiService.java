package com.example.myapplication.API;

import com.example.myapplication.Model.Alert;
import com.example.myapplication.Model.DepthData;
import com.example.myapplication.Model.LoginRequest;
import com.example.myapplication.Model.PoolInfo;
import com.example.myapplication.Model.RegisterRequest;
import com.example.myapplication.Model.TdsData;
import com.example.myapplication.Model.TemperatureData;
import com.example.myapplication.Model.PHData;
import com.example.myapplication.Model.UpdatePreferencesRequest;
import com.example.myapplication.Model.UpdateProfileRequest;
import com.example.myapplication.Model.UserPreferences;
import com.example.myapplication.Model.TasksData;
import com.example.myapplication.Model.TasksRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
public interface ApiService {
    @POST("/auth/login")
    Call<Map<String, String>> login(@Body LoginRequest loginRequest);

    @POST("/auth/register")
    Call<Map<String, String>> register(@Body RegisterRequest registerRequest);

    @GET("/api/temperature/{userId}")
    Call<List<TemperatureData>> getTemperaturesForUser(@Path("userId") String userId);

    @GET("/api/tds/{userId}")
    Call<List<TdsData>> getTdsForUser(@Path("userId") String userId);

    @GET("/api/depth/{userId}")
    Call<List<DepthData>> getDepthForUser(@Path("userId") String userId);

    @GET("/api/ph/{userId}")
    Call<List<PHData>> getPhForUser(@Path("userId") String userId);

    @GET("/api/user/{userId}")
    Call<Map<String, String>> getUserProfile(@Path("userId") String userId);

    @PUT("/api/user/{userId}")
    Call<Map<String, String>> updateUserProfile(
            @Path("userId") String userId,
            @Body UpdateProfileRequest request
    );

    @GET("/api/preferences/{userId}")
    Call<UserPreferences> getUserPreferences(@Path("userId") String userId);

    @PUT("/api/preferences/{userId}")
    Call<Map<String, String>> updatePreferences(
            @Path("userId") String userId,
            @Body UpdatePreferencesRequest request
    );

    @DELETE("/api/user/{userId}")
    Call<Void> deleteUser(@Path("userId") String userId);

    @GET("/api/alerts/{userId}")
    Call<List<Alert>> getAlertsForUser(@Path("userId") String userId);

    @GET("/api/pool/{userId}")
    Call<PoolInfo> getPoolInfo(@Path("userId") String userId);

    @PUT("/api/pool/{userId}")
    Call<Map<String, String>> updatePoolInfo(
            @Path("userId") String userId,
            @Body PoolInfo body
    );
    // Add this to your existing ApiService interface
    @POST("/auth/logout/{userId}")
    Call<Map<String, String>> logout(@Path("userId") String userId);
@POST("/api/tasks")
Call<TasksData> postTask(@Body TasksRequest request);

@GET("/api/tasks/{userId}")
Call<List<TasksData>> getTasksForUser(@Path("userId") String userId);

@DELETE("/api/tasks/{userId}/{taskId}")
Call<String> deleteTask(
        @Path("userId") String userId,
        @Path("taskId") Long taskId
);
}