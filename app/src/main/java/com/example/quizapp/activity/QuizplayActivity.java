package com.example.quizapp.activity;

import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizplay);

        topicId = getIntent().getStringExtra("topicId");
        if (topicId == null) {
            topicId = "history"; // Mặc định là history để test
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
                            // In câu hỏi ra Logcat để debug
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
}