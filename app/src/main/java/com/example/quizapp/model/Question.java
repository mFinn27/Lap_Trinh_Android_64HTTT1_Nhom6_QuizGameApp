package com.example.quizapp.model;
import java.util.List;

public class Question {
    private String question;
    private List<String> options;
    private int correctAnswer;

    public Question() {
    }

    public Question(String question, List<String> options, int correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }
}
