package com.example.quizapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.model.Question;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuizplayActivity extends AppCompatActivity {

    private List<Question> questionList = new ArrayList<>();
    private String topicId;

    private TextView tvQuestion;
    private RadioButton rb1, rb2, rb3, rb4;
    private RadioGroup rgAnswers;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizplay);

        tvQuestion = findViewById(R.id.tv_question);
        rb1 = findViewById(R.id.rb_answer1);
        rb2 = findViewById(R.id.rb_answer2);
        rb3 = findViewById(R.id.rb_answer3);
        rb4 = findViewById(R.id.rb_answer4);
        rgAnswers = findViewById(R.id.rg_answers);

        topicId = getIntent().getStringExtra("topicId");
        if (topicId == null) {
            topicId = "history";
        }

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
                            Toast.makeText(QuizplayActivity.this, "Tải được " + questionList.size() + " câu hỏi!", Toast.LENGTH_SHORT).show();
                            for (int i = 0; i < questionList.size(); i++) {
                                Question q = questionList.get(i);
                                Log.d("QuizDebug", "Câu hỏi " + (i + 1) + ": " + q.getQuestion());
                                Log.d("QuizDebug", "Lựa chọn: " + q.getOptions());
                                Log.d("QuizDebug", "Đáp án đúng: " + q.getCorrectAnswer());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QuizplayActivity.this, "Lỗi tải câu hỏi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Firebase", "Error: " + error.getMessage());
                    }
                });
    }

    private void showNextQuestion() {
        if (currentIndex >= questionList.size()) {
            Toast.makeText(this, "Hết câu hỏi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Question current = questionList.get(currentIndex);
        tvQuestion.setText(current.getQuestion());

        List<String> options = current.getOptions();
        rb1.setText(options.get(0));
        rb2.setText(options.get(1));
        rb3.setText(options.get(2));
        rb4.setText(options.get(3));

        // Xóa lựa chọn trước đó
        rgAnswers.clearCheck();
        currentIndex++;
    }
}