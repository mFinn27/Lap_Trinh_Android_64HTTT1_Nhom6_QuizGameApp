package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizplayActivity extends AppCompatActivity {

    private TextView tvQuestion, tvTimer, tvHighScore, tvQuestionsAnswered;
    private RadioButton rb1, rb2, rb3, rb4;
    private RadioGroup rgAnswers;
    private CardView cardGameOver;
    private Button btnPlayAgain, btnBack;
    private ImageButton btnBackMain;
    private List<Question> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int correctCount = 0;
    private int totalQuestionsAnswered = 0;
    private String topicId;
    private CountDownTimer countDownTimer;
    private static final long TIME_PER_QUESTION = 20000; // 20 giây mỗi câu hỏi
    private static final String TAG = "QuizplayActivity";
    private boolean isActivityActive = true;
    private String currentUserId;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizplay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_play), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo views
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
        btnBackMain = findViewById(R.id.button_back);

        // Lấy topicId từ Intent
        topicId = getIntent().getStringExtra("topicId");
        if (topicId == null) {
            Log.e(TAG, "No topicId provided in Intent");
            Toast.makeText(this, "Error: Topic not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy thông tin người dùng hiện tại
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUserId == null) {
            Log.w(TAG, "No user logged in");
            Toast.makeText(this, "Vui lòng đăng nhập để lưu điểm", Toast.LENGTH_SHORT).show();
        } else {
            loadUsername();
        }

        // Xử lý các nút
        btnBackMain.setOnClickListener(v -> finish());
        btnPlayAgain.setOnClickListener(v -> restartGame());
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(QuizplayActivity.this, SelectionTopicActivity.class);
            startActivity(intent);
            finish();
        });

        // Tải câu hỏi
        loadQuestionsFromFirebase();
    }

    private void loadUsername() {
        DatabaseReference userRef = FirebaseUtils.getDatabase().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("username").getValue(String.class);
                    if (currentUsername == null) {
                        currentUsername = "Anonymous";
                        Log.w(TAG, "No username found, using default: Anonymous");
                    }
                } else {
                    currentUsername = "Anonymous";
                    Log.w(TAG, "User data not found, using default: Anonymous");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading username: " + error.getMessage());
                currentUsername = "Anonymous";
            }
        });
    }

    private void loadQuestionsFromFirebase() {
        FirebaseUtils.getQuestionsRef(topicId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isActivityActive) return;
                questionList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String questionText = ds.child("question").getValue(String.class);
                    Long correctAnswer = ds.child("correctAnswer").getValue(Long.class);
                    List<String> options = new ArrayList<>();
                    DataSnapshot optionsSnap = ds.child("options");
                    // Tải options theo thứ tự 0, 1, 2, 3
                    for (int i = 0; i < 4; i++) {
                        String optionText = optionsSnap.child(String.valueOf(i)).getValue(String.class);
                        if (optionText != null) {
                            options.add(optionText);
                        }
                    }
                    if (questionText != null && correctAnswer != null && options.size() == 4 && correctAnswer >= 0 && correctAnswer <= 3) {
                        questionList.add(new Question(questionText, options, correctAnswer.intValue()));
                        Log.d(TAG, "Loaded question: " + questionText + ", correctAnswer: " + correctAnswer + ", options: " + options);
                    } else {
                        Log.w(TAG, "Invalid question data for key: " + ds.getKey() + ", question: " + questionText + ", correctAnswer: " + correctAnswer + ", options size: " + options.size());
                    }
                }
                if (questionList.isEmpty()) {
                    Log.e(TAG, "No valid questions found for topic: " + topicId);
                    Toast.makeText(QuizplayActivity.this, "Không có câu hỏi trong chủ đề này!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.d(TAG, "Loaded " + questionList.size() + " questions");
                    Collections.shuffle(questionList);
                    currentIndex = 0;
                    correctCount = 0;
                    totalQuestionsAnswered = 0;
                    showNextQuestion();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isActivityActive) return;
                Log.e(TAG, "Firebase error: " + error.getMessage());
                Toast.makeText(QuizplayActivity.this, "Lỗi tải câu hỏi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void showNextQuestion() {
        if (!isActivityActive) return;
        if (currentIndex >= questionList.size()) {
            Log.d(TAG, "No more questions, showing game over");
            showGameOver();
            return;
        }

        try {
            Question current = questionList.get(currentIndex);
            if (current.getQuestion() == null || current.getOptions() == null || current.getOptions().size() != 4) {
                Log.e(TAG, "Invalid question data at index " + currentIndex);
                showGameOver();
                return;
            }
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
                if (!isActivityActive) return;
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                checkAnswer(checkedId, current.getCorrectAnswer());
            });
        } catch (Exception e) {
            Log.e(TAG, "Error displaying question at index " + currentIndex, e);
            Toast.makeText(this, "Lỗi hiển thị câu hỏi", Toast.LENGTH_SHORT).show();
            showGameOver();
        }
    }

    private void startTimer() {
        if (!isActivityActive) return;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isActivityActive) return;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d",
                        millisUntilFinished / 1000 / 60, millisUntilFinished / 1000 % 60));
            }

            @Override
            public void onFinish() {
                if (!isActivityActive) return;
                Log.d(TAG, "Time's up for question " + currentIndex);
                Toast.makeText(QuizplayActivity.this, "Hết thời gian!", Toast.LENGTH_SHORT).show();
                checkAnswer(-1, questionList.get(currentIndex).getCorrectAnswer());
            }
        }.start();
    }

    private void checkAnswer(int checkedId, int correctIndex) {
        if (!isActivityActive) return;
        totalQuestionsAnswered++;
        int selectedIndex = -1;
        if (checkedId == R.id.rb_answer1) selectedIndex = 0;
        else if (checkedId == R.id.rb_answer2) selectedIndex = 1;
        else if (checkedId == R.id.rb_answer3) selectedIndex = 2;
        else if (checkedId == R.id.rb_answer4) selectedIndex = 3;

        try {
            Log.d(TAG, "Selected index: " + selectedIndex + ", Correct index: " + correctIndex);
            if (selectedIndex == correctIndex) {
                correctCount++;
                Log.d(TAG, "Correct answer, moving to next question. Current score: " + correctCount);
                Toast.makeText(this, "Đúng!", Toast.LENGTH_SHORT).show();
                currentIndex++;
                showNextQuestion();
            } else {
                String correctOption = questionList.get(currentIndex).getOptions().get(correctIndex);
                Log.d(TAG, "Wrong answer, showing game over. Correct answer: " + correctOption);
                Toast.makeText(this, "Sai! Đáp án đúng: " + correctOption, Toast.LENGTH_SHORT).show();
                showGameOver();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking answer at index " + currentIndex, e);
            Toast.makeText(this, "Lỗi kiểm tra đáp án", Toast.LENGTH_SHORT).show();
            showGameOver();
        }
    }

    private void showGameOver() {
        if (!isActivityActive) return;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        cardGameOver.setVisibility(View.VISIBLE);
        int finalScore = correctCount * 10;
        tvHighScore.setText("Điểm: " + finalScore);
        tvQuestionsAnswered.setText("Số câu trả lời: " + totalQuestionsAnswered);
        enableOptions(false);
        rgAnswers.setOnCheckedChangeListener(null);
        Log.d(TAG, "Game over displayed. Score: " + finalScore + ", Questions answered: " + totalQuestionsAnswered);

        // Cập nhật điểm lên Firebase
        if (currentUserId != null && currentUsername != null) {
            updateLeaderboard(finalScore);
        }
    }

    private void updateLeaderboard(int newScore) {
        DatabaseReference leaderboardRef = FirebaseUtils.getLeaderboardRef().child(currentUserId);
        leaderboardRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isActivityActive) return;
                Long currentHighScore = snapshot.child("highScore").getValue(Long.class);
                if (currentHighScore == null || newScore > currentHighScore) {
                    leaderboardRef.child("highScore").setValue(newScore);
                    leaderboardRef.child("username").setValue(currentUsername)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Leaderboard updated: userId=" + currentUserId + ", score=" + newScore);
                                Toast.makeText(QuizplayActivity.this, "Điểm cao đã được cập nhật!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update leaderboard: " + e.getMessage());
                                Toast.makeText(QuizplayActivity.this, "Lỗi cập nhật điểm cao", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isActivityActive) return;
                Log.e(TAG, "Error reading leaderboard: " + error.getMessage());
                Toast.makeText(QuizplayActivity.this, "Lỗi đọc dữ liệu bảng xếp hạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableOptions(boolean enable) {
        if (!isActivityActive) return;
        rb1.setEnabled(enable);
        rb2.setEnabled(enable);
        rb3.setEnabled(enable);
        rb4.setEnabled(enable);
    }

    private void restartGame() {
        if (!isActivityActive) return;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        cardGameOver.setVisibility(View.GONE);
        currentIndex = 0;
        correctCount = 0;
        totalQuestionsAnswered = 0;
        Collections.shuffle(questionList);
        rgAnswers.clearCheck();
        enableOptions(true);
        showNextQuestion();
        Log.d(TAG, "Game restarted");
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        if (questionList != null && !questionList.isEmpty() && currentIndex < questionList.size()) {
            showNextQuestion();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}