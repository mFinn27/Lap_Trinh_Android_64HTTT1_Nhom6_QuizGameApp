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

        // Khởi tạo views
        btn_back = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.rv_leaderboard);
        tvCurrentUserRank = findViewById(R.id.tv_current_user_rank);
        tvCurrentUserName = findViewById(R.id.tv_current_user_name);
        tvCurrentUserScore = findViewById(R.id.tv_current_user_score);

        // Khởi tạo RecyclerView
        entries = new ArrayList<>();
        adapter = new LeaderboardAdapter(entries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Lấy currentUserId
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Xử lý nút back
        btn_back.setOnClickListener(v -> {
            startActivity(new Intent(LeaderboardActivity.this, MainMenuActivity.class));
            finish();
        });

        // Tải dữ liệu bảng xếp hạng
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
                Collections.reverse(entries); // Đảo ngược để xếp từ cao đến thấp
                adapter.notifyDataSetChanged();

                // Cập nhật thông tin người dùng hiện tại
                updateCurrentUserInfo();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    private void updateCurrentUserInfo() {
        if (currentUserId == null) {
            // Ẩn footer nếu người dùng chưa đăng nhập
            findViewById(R.id.footer_card).setVisibility(View.GONE);
            return;
        }

        // Hiển thị footer
        findViewById(R.id.footer_card).setVisibility(View.VISIBLE);

        // Tìm thông tin người dùng hiện tại
        FirebaseUtils.getLeaderboardRef().child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    LeaderboardEntry currentUserEntry = snapshot.getValue(LeaderboardEntry.class);
                    if (currentUserEntry != null) {
                        currentUserEntry.uid = snapshot.getKey();
                        // Tính thứ hạng
                        int rank = 1;
                        for (LeaderboardEntry entry : entries) {
                            if (entry.highScore > currentUserEntry.highScore) {
                                rank++;
                            } else if (entry.highScore == currentUserEntry.highScore && !entry.uid.equals(currentUserId)) {
                                // Nếu điểm bằng nhau, so sánh uid để đảm bảo thứ hạng chính xác
                                rank++;
                            }
                        }
                        // Cập nhật giao diện footer
                        tvCurrentUserRank.setText(String.valueOf(rank));
                        tvCurrentUserName.setText(currentUserEntry.username != null ? currentUserEntry.username : "Anonymous");
                        tvCurrentUserScore.setText(currentUserEntry.highScore + " điểm");
                    } else {
                        // Không có dữ liệu, hiển thị mặc định
                        tvCurrentUserRank.setText("?");
                        tvCurrentUserName.setText("Anonymous");
                        tvCurrentUserScore.setText("0 điểm");
                    }
                } else {
                    // Người dùng chưa có điểm, hiển thị mặc định
                    tvCurrentUserRank.setText("?");
                    tvCurrentUserName.setText("Anonymous");
                    tvCurrentUserScore.setText("0 điểm");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ẩn footer nếu có lỗi
                findViewById(R.id.footer_card).setVisibility(View.GONE);
            }
        });
    }
}