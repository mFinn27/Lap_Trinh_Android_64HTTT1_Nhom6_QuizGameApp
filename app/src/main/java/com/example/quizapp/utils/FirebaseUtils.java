package com.example.quizapp.utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class FirebaseUtils {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance(
            "https://quizapp-game-3bc33-default-rtdb.asia-southeast1.firebasedatabase.app/"
    );
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final Gson gson = new Gson();

    public static FirebaseDatabase getDatabase() {
        return database;
    }

    public static DatabaseReference getQuestionsRef(String topicId) {
        return database.getReference("topics").child(topicId).child("questions");
    }

    public static DatabaseReference getLeaderboardRef() {
        return database.getReference("leaderboard");
    }

    public static FirebaseAuth getAuth() {
        return auth;
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}