package com.example.quizapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.activity.AddQuestionActivity;
import com.example.quizapp.activity.EditTopicActivity;
import com.example.quizapp.activity.QuizplayActivity;
import com.example.quizapp.model.Topic;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private List<Topic> topics;
    private Context context;
    private boolean isAdmin = false;
    private OnTopicActionListener listener;

    public interface OnTopicActionListener {
        void onDeleteTopic(Topic topic);
        void onEditTopic(Topic topic);
    }

    public TopicAdapter(Context context, List<Topic> topics, OnTopicActionListener listener) {
        this.context = context;
        this.topics = topics;
        this.listener = listener;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic_item, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topics.get(position);
        holder.tvTopicName.setText(topic.getName());

        // Hiển thị icon
        String iconName = topic.getIcon() != null ? topic.getIcon() : "ic_topic_animal";
        int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        holder.ivTopicIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_topic_animal);

        // Xử lý click vào item để chơi quiz
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, QuizplayActivity.class);
            intent.putExtra("topicId", topic.getId());
            intent.putExtra("topicName", topic.getName());
            context.startActivity(intent);
        });

        // Xử lý nút Add Question
        holder.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddQuestionActivity.class);
            intent.putExtra("topicId", topic.getId());
            context.startActivity(intent);
        });

        // Xử lý nút Edit Topic
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTopic(topic);
            }
        });

        // Xử lý nút Delete Topic
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTopic(topic);
            }
        });

        // Ẩn/hiện các nút dựa trên quyền admin
        holder.btnAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnEdit.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTopicIcon;
        TextView tvTopicName;
        ImageButton btnAdd, btnEdit, btnDelete;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTopicIcon = itemView.findViewById(R.id.iv_topic_icon);
            tvTopicName = itemView.findViewById(R.id.tv_topic_name);
            btnAdd = itemView.findViewById(R.id.btn_add_question);
            btnEdit = itemView.findViewById(R.id.btn_edit_topic);
            btnDelete = itemView.findViewById(R.id.btn_delete_topic);
        }
    }
}