package com.example.quizapp.model;

public class Topic {
    private String name;
    private String id;

    public Topic() {}

    public Topic(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}