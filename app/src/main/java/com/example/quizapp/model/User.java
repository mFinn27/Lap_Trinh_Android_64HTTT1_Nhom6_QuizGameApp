package com.example.quizapp.model;

public class User {
    public String username;
    public String email;
    public String role;

    public User() {}

    public User(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
