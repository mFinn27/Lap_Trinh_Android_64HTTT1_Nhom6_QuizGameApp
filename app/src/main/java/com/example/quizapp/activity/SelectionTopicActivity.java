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

        // Khởi tạo views
        btnBack = findViewById(R.id.btn_back);
        rvTopics = findViewById(R.id.rv_topics);
        fabAddTopic = findViewById(R.id.fab_add_topic);

        // Cấu hình RecyclerView
        rvTopics.setLayoutManager(new LinearLayoutManager(this));
        topicAdapter = new TopicAdapter(this,topicList);
        rvTopics.setAdapter(topicAdapter);

        // Khởi tạo Firebase reference
        topicsRef = FirebaseUtils.getDatabase().getReference("topics");

        // Xử lý sự kiện click
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        });

        fabAddTopic.setOnClickListener(v -> showAddTopicDialog());

        // Load danh sách topics
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
        // Inflate layout cho dialog
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_topic, null);

        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Add New Topic");

        // Ánh xạ views trong dialog
        EditText etTopicName = dialogView.findViewById(R.id.et_topic_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Tạo và hiển thị dialog
        AlertDialog dialog = builder.create();

        // Xử lý nút Save
        btnSave.setOnClickListener(v -> {
            String topicName = etTopicName.getText().toString().trim();
            if (TextUtils.isEmpty(topicName)) {
                etTopicName.setError("Please enter topic name");
                return;
            }

            // Tạo topic mới
            String topicId = topicsRef.push().getKey();
            if (topicId == null) {
                Toast.makeText(this, "Error generating topic ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu vào Firebase
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

        // Xử lý nút Cancel
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}