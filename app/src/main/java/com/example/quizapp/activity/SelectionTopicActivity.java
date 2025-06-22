package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.adapter.TopicAdapter;
import com.example.quizapp.model.Topic;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectionTopicActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvTopics;
    private TopicAdapter topicAdapter;
    private List<Topic> topicList = new ArrayList<>();
    private FloatingActionButton fab_add_topic;
    private FloatingActionButton fabAddTopic;
    private DatabaseReference topicsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selection_topic);

        btnBack = findViewById(R.id.btn_back);
        rvTopics = findViewById(R.id.rv_topics);

        fab_add_topic = findViewById(R.id.fab_add_topic);
        fabAddTopic = findViewById(R.id.fab_add_topic);

        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        topicAdapter = new TopicAdapter(this,topicList);
        rvTopics.setAdapter(topicAdapter);

        btn_back.setOnClickListener(v -> startActivity(new Intent(this, MainMenuActivity.class)));
        checkUserRoleAndLoadTopics();
        topicsRef = FirebaseUtils.getDatabase().getReference("topics");

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        });

        fabAddTopic.setOnClickListener(v -> showAddTopicDialog());

        loadTopicsFromFirebase();
    }

    private void loadTopicsFromFirebase() {
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
                Toast.makeText(SelectionTopicActivity.this,
                        "Failed to load topics: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTopicDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_topic, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Add New Topic");

        EditText etTopicName = dialogView.findViewById(R.id.et_topic_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String topicName = etTopicName.getText().toString().trim();
            if (TextUtils.isEmpty(topicName)) {
                etTopicName.setError("Please enter topic name");
                return;
            }

            String topicId = topicsRef.push().getKey();
            if (topicId == null) {
                Toast.makeText(this, "Error generating topic ID", Toast.LENGTH_SHORT).show();
                return;
            }

            topicsRef.child(topicId).child("name").setValue(topicName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(SelectionTopicActivity.this,
                                "Topic added successfully",
                                Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SelectionTopicActivity.this,
                                "Failed to add topic: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

    }
    private void checkUserRoleAndLoadTopics() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseUtils.getDatabase().getReference("users").child(currentUser.getUid());

        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                boolean isAdmin = "admin".equalsIgnoreCase(role);
                topicAdapter.setAdmin(isAdmin);
                if (fab_add_topic != null) {
                    fab_add_topic.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                }
                rvTopics.setAdapter(topicAdapter);
                loadTopicsFromFirebase();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}