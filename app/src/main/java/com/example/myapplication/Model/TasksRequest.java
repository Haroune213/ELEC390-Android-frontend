package com.example.myapplication.Model;

public class TasksRequest {
    private String user_id;
    private String task_description;
    private int time_separation;
    private int frequency;
    private boolean completed;

    public TasksRequest(String user_id, String task_description, int time_separation, int frequency, boolean completed) {
        this.user_id = user_id;
        this.task_description = task_description;
        this.time_separation = time_separation;
        this.frequency = frequency;
        this.completed = completed;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getTask_description() {
        return task_description;
    }

    public int getTime_separation() {
        return time_separation;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isCompleted() {
        return completed;
    }
}