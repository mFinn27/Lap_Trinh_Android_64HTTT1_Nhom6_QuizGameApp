package com.example.quizapp.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quizapp.R;
import com.example.quizapp.model.Question;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuizplayActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTimer, tvHighScore, tvQuestionsAnswered;
    private RadioButton rb1, rb2, rb3, rb4;
    private RadioGroup rgAnswers;
    private CardView cardGameOver;
    private Button btnPlayAgain, btnBack;

    private List<Question> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int correctCount = 0;
    private Set<Integer> askedIndices = new HashSet<>();
    private String topicId;

    private CountDownTimer countDownTimer;
    private final long timePerQuestion = 20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quizplay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_play), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvQuestion = findViewById(R.id.tv_question);
        tvTimer = findViewById(R.id.tv_timer);
        tvHighScore = findViewById(R.id.tv_high_score);
        tvQuestionsAnswered = findViewById(R.id.tv_questions_answered);
        rb1 = findViewById(R.id.rb_answer1);
        rb2 = findViewById(R.id.rb_answer2);
        rb3 = findViewById(R.id.rb_answer3);
        rb4 = findViewById(R.id.rb_answer4);
        rgAnswers = findViewById(R.id.rg_answers);
        cardGameOver = findViewById(R.id.card_game_over);
        btnPlayAgain = findViewById(R.id.btn_play_again);
        btnBack = findViewById(R.id.btn_back_to_topics);

        topicId = getIntent().getStringExtra("topicId");
        if (topicId == null) {
            topicId = "history";
        }

        btnBack.setOnClickListener(v -> finish());

        loadQuestionsFromFirebase();
    }

    private void loadQuestionsFromFirebase() {
        FirebaseUtils.getQuestionsRef(topicId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        questionList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Question q = ds.getValue(Question.class);
                            if (q != null && q.getOptions() != null && q.getOptions().size() >= 4) {
                                questionList.add(q);
                            }
                        }

                        if (questionList.isEmpty()) {
                            Toast.makeText(QuizplayActivity.this, "Không có câu hỏi trong chủ đề này!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Collections.shuffle(questionList);
                            currentIndex = 0;
                            correctCount = 0;
                            askedIndices.clear();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QuizplayActivity.this, "Lỗi tải câu hỏi", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Error: " + error.getMessage());
                    }
                });
    }
}