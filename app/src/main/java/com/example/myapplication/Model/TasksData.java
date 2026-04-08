package com.example.myapplication.Model;

public class TasksData {
    private Long id;
    private String userId;
    private String taskDescription;
    private int timeSeparation;
    private int frequency;
    private boolean completed;

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public int getTimeSeparation() {
        return timeSeparation;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isCompleted() {
        return completed;
    }
}