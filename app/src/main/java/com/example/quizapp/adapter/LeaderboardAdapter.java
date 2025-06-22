package com.example.quizapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp.R;
import com.example.quizapp.model.LeaderboardEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private final List<LeaderboardEntry> entries;
    private final String currentUserId;

    public LeaderboardAdapter(List<LeaderboardEntry> entries) {
        this.entries = entries;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = entries.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvUsername.setText(entry.username);
        holder.tvScore.setText(entry.highScore + " điểm");
        if (entry.uid != null && entry.uid.equals(currentUserId)) {
            holder.itemView.setBackgroundColor(0xFFE6E6FA);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvScore = itemView.findViewById(R.id.tv_score);
        }
    }
}