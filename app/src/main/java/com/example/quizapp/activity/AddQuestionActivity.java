package com.example.quizapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quizapp.R;
import com.example.quizapp.model.Question;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.util.Arrays;
import java.util.List;

public class AddQuestionActivity extends AppCompatActivity {

    private TextInputEditText etQuestion, etAnswer1, etAnswer2, etAnswer3, etAnswer4;
    private Spinner spCorrectAnswer;
    private String topicId;

    private ImageButton btnback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_question);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_addquestion), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        topicId = getIntent().getStringExtra("topicId");
        if (topicId == null) {
            Toast.makeText(this, "No topic selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etQuestion = findViewById(R.id.et_question);
        etAnswer1 = findViewById(R.id.et_answer1);
        etAnswer2 = findViewById(R.id.et_answer2);
        etAnswer3 = findViewById(R.id.et_answer3);
        etAnswer4 = findViewById(R.id.et_answer4);
        spCorrectAnswer = findViewById(R.id.sp_correct_answer);
        findViewById(R.id.btn_save_question).setOnClickListener(this::saveQuestion);
        btnback = findViewById(R.id.btn_back);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddQuestionActivity.this, SelectionTopicActivity.class));
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Answer 1", "Answer 2", "Answer 3", "Answer 4"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCorrectAnswer.setAdapter(adapter);
    }

    private void saveQuestion(View view) {
        String question = etQuestion.getText().toString().trim();
        String ans1 = etAnswer1.getText().toString().trim();
        String ans2 = etAnswer2.getText().toString().trim();
        String ans3 = etAnswer3.getText().toString().trim();
        String ans4 = etAnswer4.getText().toString().trim();
        int correctAnswer = spCorrectAnswer.getSelectedItemPosition();

        if (question.isEmpty() || ans1.isEmpty() || ans2.isEmpty() ||
                ans3.isEmpty() || ans4.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> options = Arrays.asList(ans1, ans2, ans3, ans4);
        Question newQuestion = new Question(question, options, correctAnswer);

        DatabaseReference questionsRef = FirebaseUtils.getQuestionsRef(topicId);
        String newKey = questionsRef.push().getKey();

        if (newKey == null) {
            Toast.makeText(this, "Failed to generate key", Toast.LENGTH_SHORT).show();
            return;
        }
        questionsRef.child(newKey).setValue(newQuestion)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Question added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}