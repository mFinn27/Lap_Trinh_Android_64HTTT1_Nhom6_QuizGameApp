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
    private FloatingActionButton fabAddTopic;
    private DatabaseReference topicsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selection_topic);

        btnBack = findViewById(R.id.btn_back);
        rvTopics = findViewById(R.id.rv_topics);
        fabAddTopic = findViewById(R.id.fab_add_topic);

        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        topicAdapter = new TopicAdapter(this, topicList, new TopicAdapter.OnTopicActionListener() {
            @Override
            public void onDeleteTopic(Topic topic) {
                showDeleteConfirmationDialog(topic);
            }

            @Override
            public void onEditTopic(Topic topic) {
                Intent intent = new Intent(SelectionTopicActivity.this, EditTopicActivity.class);
                intent.putExtra("topic", topic);
                startActivity(intent);
            }
        });
        rvTopics.setAdapter(topicAdapter);

        topicsRef = FirebaseUtils.getDatabase().getReference("topics");

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        });

        fabAddTopic.setOnClickListener(v -> showAddTopicDialog());

        checkUserRoleAndLoadTopics();
    }

    private void loadTopicsFromFirebase() {
        topicsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topicList.clear();
                for (DataSnapshot topicSnap : snapshot.getChildren()) {
                    String topicId = topicSnap.getKey();
                    String name = topicSnap.child("name").getValue(String.class);
                    String icon = topicSnap.child("icon").getValue(String.class);
                    if (topicId != null && name != null) {
                        Topic topic = new Topic(topicId, name, icon != null ? icon : "ic_topic_animal");
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

            Topic newTopic = new Topic(topicId, topicName, "ic_topic_animal");
            topicsRef.child(topicId).setValue(newTopic)
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
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void checkUserRoleAndLoadTopics() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            topicAdapter.setAdmin(false);
            fabAddTopic.setVisibility(View.GONE);
            loadTopicsFromFirebase();
            return;
        }

        DatabaseReference userRef = FirebaseUtils.getDatabase().getReference("users").child(currentUser.getUid());

        userRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                boolean isAdmin = "admin".equalsIgnoreCase(role);
                topicAdapter.setAdmin(isAdmin);
                fabAddTopic.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                loadTopicsFromFirebase();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectionTopicActivity.this,
                        "Error checking role: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                topicAdapter.setAdmin(false);
                fabAddTopic.setVisibility(View.GONE);
                loadTopicsFromFirebase();
            }
        });
    }

    private void showDeleteConfirmationDialog(Topic topic) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Topic")
                .setMessage("Are you sure you want to delete the topic '" + topic.getName() + "'? This will also delete all related questions.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTopicFromFirebase(topic);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTopicFromFirebase(Topic topic) {
        topicsRef.child(topic.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SelectionTopicActivity.this,
                            "Topic deleted successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SelectionTopicActivity.this,
                            "Failed to delete topic: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}