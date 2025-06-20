package com.example.quizapp.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_quizplay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
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

        btnPlayAgain.setOnClickListener(v -> restartGame());
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
                            showNextQuestion();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QuizplayActivity.this, "Lỗi tải câu hỏi", Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Error: " + error.getMessage());
                    }
                });
    }

    private void showNextQuestion() {
        if (currentIndex >= questionList.size()) {
            showGameOver();
            return;
        }

        Question current = questionList.get(currentIndex);
        tvQuestion.setText(current.getQuestion());

        List<String> options = current.getOptions();
        rb1.setText(options.get(0));
        rb2.setText(options.get(1));
        rb3.setText(options.get(2));
        rb4.setText(options.get(3));

        rgAnswers.setOnCheckedChangeListener(null);
        rgAnswers.clearCheck();
        enableOptions(true);

        startTimer();
        rgAnswers.setOnCheckedChangeListener((group, checkedId) -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            checkAnswer(checkedId, current.getCorrectAnswer());
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timePerQuestion, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("00:" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizplayActivity.this, "Hết thời gian!", Toast.LENGTH_SHORT).show();
                showGameOver();
            }
        }.start();
    }

    private void checkAnswer(int checkedId, int correctIndex) {
        int selectedIndex = -1;

        if (checkedId == R.id.rb_answer1) selectedIndex = 0;
        else if (checkedId == R.id.rb_answer2) selectedIndex = 1;
        else if (checkedId == R.id.rb_answer3) selectedIndex = 2;
        else if (checkedId == R.id.rb_answer4) selectedIndex = 3;

        if (selectedIndex == correctIndex) {
            correctCount++;
            currentIndex++;
            showNextQuestion();
        } else {
            showGameOver();
        }
    }
    private void showGameOver() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (cardGameOver.getVisibility() != View.VISIBLE) {
            cardGameOver.setVisibility(View.VISIBLE);
            tvHighScore.setText("Điểm: " + correctCount);
            tvQuestionsAnswered.setText("Câu đúng: " + correctCount);
        }
        enableOptions(false);
        rgAnswers.setOnCheckedChangeListener(null);
    }

    private void enableOptions(boolean enable) {
        for (int i = 0; i < rgAnswers.getChildCount(); i++) {
            rgAnswers.getChildAt(i).setEnabled(enable);
        }
    }

    private void restartGame() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (cardGameOver.getVisibility() == View.VISIBLE) {
            cardGameOver.setVisibility(View.GONE);
        }
        currentIndex = 0;
        correctCount = 0;
        askedIndices.clear();
        Collections.shuffle(questionList);
        rgAnswers.clearCheck();
        enableOptions(true);

        rgAnswers.setOnCheckedChangeListener(null);
        showNextQuestion();
    }
}