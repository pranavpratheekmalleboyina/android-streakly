package com.pranav.streakly.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pranav.streakly.R;
import com.pranav.streakly.models.Habit;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<Habit> habitList;

    public LeaderboardAdapter(List<Habit> habitList) {
        this.habitList = habitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.habit_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.tvRank.setText("" + (position + 1));
        holder.tvHabitName.setText(habit.getName());
        holder.tvStreakCount.setText(" " + habit.getStreakCount());

        // Highlight top 1 habit
        if (position == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFE082")); // Amber for gold
        } else {
            holder.itemView.setBackgroundResource(R.drawable.stat_card_bg); // default
        }
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName, tvStreakCount ,tvRank;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvStreakCount = itemView.findViewById(R.id.tvStreakCount);
        }
    }
}
