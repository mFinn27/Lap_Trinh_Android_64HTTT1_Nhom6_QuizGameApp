package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.adapter.TopicAdapter;
import com.example.quizapp.model.Topic;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectionTopicActivity extends AppCompatActivity {

    private ImageButton btn_back;
    private RecyclerView rvTopics;
    private TopicAdapter topicAdapter;
    private List<Topic> topicList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selection_topic);

        btn_back = findViewById(R.id.btn_back);
        rvTopics = findViewById(R.id.rv_topics);

        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        topicAdapter = new TopicAdapter(topicList);
        rvTopics.setAdapter(topicAdapter);

        btn_back.setOnClickListener(v -> startActivity(new Intent(this, MainMenuActivity.class)));

        loadTopicsFromFirebase();
    }

    private void loadTopicsFromFirebase() {
        DatabaseReference topicsRef = FirebaseUtils.getDatabase().getReference("topics");

        topicsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topicList.clear();

                for (DataSnapshot topicSnap : snapshot.getChildren()) {
                    String topicId = topicSnap.getKey();
                    String name = topicSnap.child("name").getValue(String.class);

                    if (topicId != null && name != null) {
                        Topic topic = new Topic(topicId, name);
                        topicList.add(topic);
                    }
                }
                topicAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}