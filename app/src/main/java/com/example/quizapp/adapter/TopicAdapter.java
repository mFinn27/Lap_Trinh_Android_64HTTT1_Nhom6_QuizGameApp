package com.example.quizapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
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

        String iconName = topic.getIcon() != null ? topic.getIcon() : "ic_topic_animal";
        int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
        holder.ivTopicIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_topic_animal);

        holder.itemView.setOnClickListener(v -> {
            showReadyDialog(topic.getId(), topic.getName());
        });

        holder.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddQuestionActivity.class);
            intent.putExtra("topicId", topic.getId());
            context.startActivity(intent);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditTopic(topic);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteTopic(topic);
            }
        });

        holder.btnAdd.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnEdit.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        holder.btnDelete.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void showReadyDialog(String topicId, String topicName) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_ready);
        dialog.setCancelable(false);

        TextView tvCountdown = dialog.findViewById(R.id.tv_countdown);

        MediaPlayer countdownPlayer = MediaPlayer.create(context, R.raw.countdown_3s);
        if (countdownPlayer != null) {
            countdownPlayer.setVolume(1.0f, 1.0f); // Đặt âm lượng tối đa
            countdownPlayer.start();
        }

        new CountDownTimer(4000, 1000) { // 4 giây để bao gồm cả "Go!"
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvCountdown.setText(String.valueOf(secondsLeft));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Go!");
                new android.os.Handler().postDelayed(() -> {
                    Intent intent = new Intent(context, QuizplayActivity.class);
                    intent.putExtra("topicId", topicId);
                    intent.putExtra("topicName", topicName);
                    context.startActivity(intent);
                    dialog.dismiss();
                    // Giải phóng tài nguyên MediaPlayer
                    if (countdownPlayer != null) {
                        countdownPlayer.release();
                    }
                }, 500);
            }
        }.start();

        dialog.show();
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