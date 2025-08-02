package com.pranav.streakly;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pranav.streakly.adapters.LeaderboardAdapter;
import com.pranav.streakly.helpers.StatsHelper;
import com.pranav.streakly.models.Habit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatsActivity extends AppCompatActivity {
    private TextView tvTotalHabits, tvTotalLogs, tvTopHabit, tvCurrentStreak;
    private FlexboxLayout layoutBadges;
    private BarChart barChart;
    private LineChart lineChart;
    private PieChart pieChart;
    private LinearLayout leaderboardLayout;
    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter leaderboardAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvTotalHabits = findViewById(R.id.tvTotalHabits);
        tvTotalLogs = findViewById(R.id.tvTotalLogs);
        tvTopHabit = findViewById(R.id.tvTopHabit);
        tvCurrentStreak = findViewById(R.id.tvCurrentStreak);
        layoutBadges = findViewById(R.id.layoutBadges);
        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);

        leaderboardLayout = findViewById(R.id.leaderboardLayout);
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));


        // Optional: Style the charts
        lineChart.getDescription().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterText("Streak Share");
        pieChart.setCenterTextSize(16f);

        loadStats();
        fetchHabitStatsAndVisualize(); // fetch and visualize data
    }

    private void loadStats() {
        StatsHelper.computeStats(this, new StatsHelper.StatsCallback() {
            @Override
            public void onStatsComputed(int totalHabits, int totalLogs, int bestStreak, List<Habit> habits) {
                tvTotalHabits.setText(String.valueOf(totalHabits));
                tvTotalLogs.setText(String.valueOf(totalLogs));

                Habit topHabit = null;
                for (Habit h : habits) {
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
                displayBarChart(habits);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StatsActivity.this, "Failed to load stats: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlockBadges(int totalLogs, int bestStreak, int totalHabits) {
        //layoutBadges.removeAllViews();

        if (totalLogs >= 20) addBadgeView("🏅 Consistency Champ", "20 logs sessions in!");
        if (bestStreak >= 10) addBadgeView("🔥 Habit Conqueror", "10-log streak achieved!");
        if (totalHabits >= 5) addBadgeView("🧠 Habit Emperor", "Created 5 habits!");
    }

    private void addBadgeView(String title, String desc) {
        TextView badge = new TextView(this);
        badge.setText(title + "\n" + desc);
        badge.setTextSize(14f);
        badge.setPadding(24, 16, 24, 16);
        badge.setBackgroundResource(R.drawable.badge_bg);
        badge.setTextColor(getColor(android.R.color.black));

        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(12, 12, 12, 12);
        badge.setLayoutParams(params);

        layoutBadges.addView(badge);
    }

    private void displayBarChart(List<Habit> habits) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> habitNames = new ArrayList<>();

        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);
            entries.add(new BarEntry(i, h.getStreakCount()));
            habitNames.add(h.getName());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Streak Count per Habit");
        dataSet.setColor(Color.parseColor("#4CAF50")); // green
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);

        // X Axis setup
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(habitNames));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45);

        // Y Axis setup
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setGranularity(1f);

        barChart.invalidate(); // refresh chart
    }

    private void fetchHabitStatsAndVisualize() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Habit> habits = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Habit habit = doc.toObject(Habit.class);
                        habits.add(habit);
                    }
                    updateChartsAndLeaderboard(habits);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch habits", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChartsAndLeaderboard(List<Habit> habits) {
        // Line chart for streak progression
        List<Entry> lineEntries = new ArrayList<>();
        int index = 0;
        for (Habit h : habits) {
            lineEntries.add(new Entry(index++, h.getStreakCount()));
        }
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Streak Count");
        lineDataSet.setValueTextSize(12f);
        lineChart.setData(new LineData(lineDataSet));
        lineChart.invalidate();

        // Pie chart for streak distribution
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Habit h : habits) {
            pieEntries.add(new PieEntry(h.getStreakCount(), h.getName()));
        }
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Streak Share");
        pieChart.setData(new PieData(pieDataSet));
        pieChart.invalidate();

        // Leaderboard (sorted by best streak)
        // Sort by streak count descending
        Collections.sort(habits, (a, b) -> Integer.compare(b.getStreakCount(), a.getStreakCount()));

        // Set top 5 for leaderboard
        List<Habit> top5 = habits.size() > 5 ? habits.subList(0, 5) : habits;

        leaderboardAdapter = new LeaderboardAdapter(top5);
        rvLeaderboard.setAdapter(leaderboardAdapter);
    }
}
