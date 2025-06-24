package com.example.quizapp.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.quizapp.R;
import com.example.quizapp.model.Topic;
import com.example.quizapp.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EditTopicActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle;
    private TextInputEditText etTopicName;
    private ImageView ivTopicIcon;
    private AppCompatButton btnChangeIcon, btnSaveTopic;
    private DatabaseReference topicsRef;
    private Topic topic;
    private String selectedIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_topic);

        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);
        etTopicName = findViewById(R.id.et_topic_name);
        ivTopicIcon = findViewById(R.id.iv_topic_icon);
        btnChangeIcon = findViewById(R.id.btn_change_icon);
        btnSaveTopic = findViewById(R.id.btn_save_topic);

        topic = (Topic) getIntent().getSerializableExtra("topic");
        if (topic == null) {
            Toast.makeText(this, "Error: Topic not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        topicsRef = FirebaseUtils.getDatabase().getReference("topics").child(topic.getId());

        etTopicName.setText(topic.getName());
        selectedIcon = topic.getIcon() != null ? topic.getIcon() : "ic_topic_animal";
        int iconResId = getResources().getIdentifier(selectedIcon, "drawable", getPackageName());
        ivTopicIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_topic_animal);

        btnBack.setOnClickListener(v -> finish());

        btnChangeIcon.setOnClickListener(v -> {
            List<String> drawableNames = getDrawableNames();
            if (drawableNames.isEmpty()) {
                Toast.makeText(this, "No icons available", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose Icon");
            builder.setItems(drawableNames.toArray(new String[0]), (dialog, which) -> {
                selectedIcon = drawableNames.get(which);
                int resId = getResources().getIdentifier(selectedIcon, "drawable", getPackageName());
                ivTopicIcon.setImageResource(resId != 0 ? resId : R.drawable.ic_topic_animal);
                Toast.makeText(this, "Icon changed to " + selectedIcon, Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });

        btnSaveTopic.setOnClickListener(v -> {
            String newTopicName = etTopicName.getText().toString().trim();
            if (TextUtils.isEmpty(newTopicName)) {
                etTopicName.setError("Please enter topic name");
                return;
            }

            topicsRef.child("name").setValue(newTopicName)
                    .addOnSuccessListener(aVoid -> {
                        // Lưu icon
                        topicsRef.child("icon").setValue(selectedIcon)
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(EditTopicActivity.this,
                                            "Topic updated successfully",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(EditTopicActivity.this,
                                            "Failed to update icon: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditTopicActivity.this,
                                "Failed to update topic: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private List<String> getDrawableNames() {
        List<String> drawableNames = new ArrayList<>();
        Field[] fields = R.drawable.class.getFields();
        for (Field field : fields) {
            try {
                // Lọc các drawable bắt đầu bằng "ic_topic" (tùy chọn)
                String name = field.getName();
                if (name.startsWith("ic_topic")) {
                    drawableNames.add(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return drawableNames;
    }
}