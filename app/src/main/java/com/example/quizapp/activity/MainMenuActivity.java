package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quizapp.R;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_mainmenu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnChooseTopic = findViewById(R.id.btn_choose_topic);
        Button btnLeaderboard = findViewById(R.id.btn_leaderboard);
        Button btnLogout = findViewById(R.id.btn_logout);

        btnChooseTopic.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SelectionTopicActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMenuActivity.this, LeaderboardActivity.class);
                startActivity(intent);
            }
        });
    }
}