package com.example.myapplication.Model;

public class UpdateProfileRequest {
    private String username;
    private String email;

    public UpdateProfileRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
}