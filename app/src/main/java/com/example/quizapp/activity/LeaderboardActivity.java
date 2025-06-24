package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private ImageButton btn_back;
    private RecyclerView recyclerView;
    private TextView tvCurrentUserRank, tvCurrentUserName, tvCurrentUserScore;
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
        tvCurrentUserRank = findViewById(R.id.tv_current_user_rank);
        tvCurrentUserName = findViewById(R.id.tv_current_user_name);
        tvCurrentUserScore = findViewById(R.id.tv_current_user_score);

        entries = new ArrayList<>();
        adapter = new LeaderboardAdapter(entries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

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
                updateCurrentUserInfo();
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void updateCurrentUserInfo() {
        if (currentUserId == null) {
            findViewById(R.id.footer_card).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.footer_card).setVisibility(View.VISIBLE);
        FirebaseUtils.getLeaderboardRef().child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    LeaderboardEntry currentUserEntry = snapshot.getValue(LeaderboardEntry.class);
                    if (currentUserEntry != null) {
                        currentUserEntry.uid = snapshot.getKey();
                        int rank = 1;
                        for (LeaderboardEntry entry : entries) {
                            if (entry.highScore > currentUserEntry.highScore) {
                                rank++;
                            } else if (entry.highScore == currentUserEntry.highScore && !entry.uid.equals(currentUserId)) {
                                rank++;
                            }
                        }
                        tvCurrentUserRank.setText(String.valueOf(rank));
                        tvCurrentUserName.setText(currentUserEntry.username != null ? currentUserEntry.username : "Anonymous");
                        tvCurrentUserScore.setText(currentUserEntry.highScore + " điểm");
                    } else {
                        tvCurrentUserRank.setText("?");
                        tvCurrentUserName.setText("Anonymous");
                        tvCurrentUserScore.setText("0 điểm");
                    }
                } else {
                    tvCurrentUserRank.setText("?");
                    tvCurrentUserName.setText("Anonymous");
                    tvCurrentUserScore.setText("0 điểm");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                findViewById(R.id.footer_card).setVisibility(View.GONE);
            }
        });
    }
}