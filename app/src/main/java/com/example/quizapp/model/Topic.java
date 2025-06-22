package com.example.quizapp.model;

import java.io.Serializable;

public class Topic implements Serializable {
    private String id;
    private String name;
    private String icon;

    public Topic() {
        // Constructor mặc định cho Firebase
    }

    public Topic(String id, String name) {
        this.id = id;
        this.name = name;
        this.icon = "ic_topic_animal"; // Icon mặc định
    }

    public Topic(String id, String name, String icon) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}