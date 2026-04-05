package com.example.myapplication.Model;

public class UserPreferences {
    private String userId;
    private Double tempMin, tempMax;
    private Double phMin, phMax;
    private Double tdsMin, tdsMax;
    private Double depthMin, depthMax;

    // Getters
    public String getUserId()    { return userId; }
    public Double getTempMin()   { return tempMin; }
    public Double getTempMax()   { return tempMax; }
    public Double getPhMin()     { return phMin; }
    public Double getPhMax()     { return phMax; }
    public Double getTdsMin()    { return tdsMin; }
    public Double getTdsMax()    { return tdsMax; }
    public Double getDepthMin()  { return depthMin; }
    public Double getDepthMax()  { return depthMax; }
}