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
import com.example.quizapp.model.Topic;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private List<Topic> topics;
    private Context context;

    public TopicAdapter(List<Topic> topics) {
        this.topics = topics;
        this.context = context;
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

        holder.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddQuestionActivity.class);
            intent.putExtra("topicId", topics.get(position).getId());
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return topics.size();
    }

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTopicIcon;
        TextView tvTopicName;
        ImageButton btnAdd;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTopicIcon = itemView.findViewById(R.id.iv_topic_icon);
            tvTopicName = itemView.findViewById(R.id.tv_topic_name);
            btnAdd = itemView.findViewById(R.id.btn_add_question);
        }
    }
}