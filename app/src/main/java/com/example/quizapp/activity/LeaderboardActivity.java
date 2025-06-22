package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.adapter.LeaderboardAdapter;
import com.example.quizapp.model.LeaderboardEntry;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private ImageButton btn_back;
    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> entries;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_leaderboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_back = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.rv_leaderboard);

        entries = new ArrayList<>();
        adapter = new LeaderboardAdapter(entries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";

        btn_back.setOnClickListener(v -> {
            startActivity(new Intent(LeaderboardActivity.this, MainMenuActivity.class));
            finish();
        });

        loadLeaderboard();
    }
    private void loadLeaderboard() {
        FirebaseUtils.getLeaderboardRef().orderByChild("highScore").limitToLast(100).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                entries.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    LeaderboardEntry entry = data.getValue(LeaderboardEntry.class);
                    if (entry != null) {
                        entry.uid = data.getKey();
                        entries.add(entry);
                    }
                }
                Collections.reverse(entries);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
}
