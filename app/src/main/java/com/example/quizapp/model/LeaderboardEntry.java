package com.example.quizapp.model;

public class LeaderboardEntry {
    public String username;
    public String uid;
    public int  highScore;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String username, int highScore) {
        this.username = username;
        this.highScore = highScore;
    }
}
