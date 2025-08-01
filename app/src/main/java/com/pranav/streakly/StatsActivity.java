package com.pranav.streakly;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pranav.streakly.helpers.StatsHelper;
import com.pranav.streakly.models.Habit;

import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private TextView tvTotalHabits, tvTotalLogs, tvAvgProgress, tvTopHabit, tvCurrentStreak;
    private LinearLayout layoutBadges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvTotalHabits = findViewById(R.id.tvTotalHabits);
        tvTotalLogs = findViewById(R.id.tvTotalLogs);
        tvAvgProgress = findViewById(R.id.tvAvgProgress);
        tvTopHabit = findViewById(R.id.tvTopHabit);
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        layoutBadges = findViewById(R.id.layoutBadges);

        loadStats();
    }

    private void loadStats() {
        StatsHelper.computeStats(this, new StatsHelper.StatsCallback() {
            @Override
            public void onStatsComputed(int totalHabits, int totalLogs, int bestStreak, List<Habit> habitList) {
                tvTotalHabits.setText(String.valueOf(totalHabits));
                tvTotalLogs.setText(String.valueOf(totalLogs));
                tvAvgProgress.setText((totalHabits > 0 ? (totalLogs / totalHabits) : 0) + "%");

                Habit topHabit = null;
                for (Habit h : habitList) {
                    if (h.getBestStreak() == bestStreak) {
                        topHabit = h;
                        break;
                    }
                }

                if (topHabit != null) {
                    tvTopHabit.setText("🏆 Top Habit: " + topHabit.getName());
                    tvCurrentStreak.setText("🔥 Current Streak: " + topHabit.getStreakCount());
                } else {
                    tvTopHabit.setText("🏆 Top Habit: None");
                    tvCurrentStreak.setText("🔥 Current Streak: 0");
                }

                unlockBadges(totalLogs, bestStreak, totalHabits);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StatsActivity.this, "Failed to load stats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlockBadges(int totalLogs, int bestStreak, int totalHabits) {
        layoutBadges.removeAllViews();

        if (totalLogs >= 20) addBadgeView("🏅 Consistency Champ", "20 days in!");
        if (bestStreak >= 10) addBadgeView("🔥 Habit Conquerer", "10-day streak achieved!");
        if (totalHabits >= 5) addBadgeView("🧠 Habit Emperor", "Created 5 habits!");
    }

    private void addBadgeView(String title, String desc) {
        TextView badge = new TextView(this);
        badge.setText(title + "\n" + desc);
        badge.setTextSize(14f);
        badge.setPadding(24, 16, 24, 16);
        badge.setBackgroundResource(R.drawable.badge_bg); // Add your own drawable
        badge.setTextColor(getColor(android.R.color.black));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 12, 12, 12);
        badge.setLayoutParams(params);

        layoutBadges.addView(badge);
    }

}
