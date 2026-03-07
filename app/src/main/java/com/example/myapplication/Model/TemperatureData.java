package com.example.myapplication.Model;

public class TemperatureData {
    private Long id;
    private String userId;
    private String time;
    private Double temperature;

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTime() {
        return time;
    }

    public Double getTemperature() {
        return temperature;
    }
}