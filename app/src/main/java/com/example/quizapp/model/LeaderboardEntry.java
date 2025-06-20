package com.example.quizapp.model;

public class LeaderboardEntry {
    public String username;
    public int score;

    public LeaderboardEntry() {}

    public LeaderboardEntry(String username, int score) {
        this.username = username;
        this.score = score;
    }
}
