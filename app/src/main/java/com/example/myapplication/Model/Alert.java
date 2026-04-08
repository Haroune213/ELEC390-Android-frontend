package com.example.myapplication.Model;

public class Alert {
    private Long id;
    private String userId;
    private String sensorType;
    private String message;
    private Double value;
    private String timestamp; // String bcs Gson treats LocalDateTime as a String
    private Boolean resolved;

    public Long getId()          { return id; }
    public String getUserId()    { return userId; }
    public String getSensorType(){ return sensorType; }
    public String getMessage()   { return message; }
    public Double getValue()     { return value; }
    public String getTimestamp() { return timestamp; }
    public Boolean getResolved() { return resolved; }
}