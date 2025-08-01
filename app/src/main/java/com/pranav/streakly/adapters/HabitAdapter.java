package com.pranav.streakly.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.SoundPool;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pranav.streakly.R;
import com.pranav.streakly.models.Habit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder>{
    private List<Habit> habitList;
    private Context context;
    private SharedPreferences badgePreferences;
    private SoundPool soundPool;
    private int soundConsistentHabitLog,soundTotalHabitLog;

    public HabitAdapter(List<Habit> habitList,Context context) {
        this.habitList = habitList;
        this.context = context;
        this.badgePreferences = context.getSharedPreferences("BadgePrefs", Context.MODE_PRIVATE);
        soundPool = new SoundPool.Builder().setMaxStreams(2).build();
        soundConsistentHabitLog = soundPool.load(context, R.raw.single_habit_consistent_reward, 1);
        soundTotalHabitLog = soundPool.load(context, R.raw.total_habit_reward, 1);
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_card, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        Habit habit = habitList.get(position);
        holder.tvHabitName.setText(habit.getName());
        holder.tvHabitGoal.setText(habit.getGoal());
        holder.tvStreak.setText("Streak: " + habit.getStreakCount() + " 🔥");
        holder.tvBestStreak.setText("Record: " + habit.getBestStreak() + " 🔥");

        holder.btnEdit.setOnClickListener(v -> {
            int truePosition = holder.getAdapterPosition();
            if (truePosition != RecyclerView.NO_POSITION){
                showEditDialog(habitList.get(truePosition), truePosition);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int truePosition = holder.getAdapterPosition();
            if (truePosition != RecyclerView.NO_POSITION) {
                deleteHabitFromFirestore(habitList.get(truePosition).getId(), truePosition);
            }
        });

        holder.btnLog.setOnClickListener(v -> {
            int truePosition = holder.getAdapterPosition();
            if (truePosition != RecyclerView.NO_POSITION) {
                habit.setStreakCount(habit.getStreakCount() + 1);
                if(habit.getStreakCount() > habit.getBestStreak()){
                    habit.setBestStreak(habit.getStreakCount());
                }
                logProgressForUser(habitList.get(truePosition).getId(), habit.getStreakCount(),habit.getBestStreak());
                notifyItemChanged(truePosition);
                checkForMilestone(habit,habitList);
            }
        });

        holder.btnResetStreak.setOnClickListener(v ->{
            int truePosition = holder.getAdapterPosition();
            if (truePosition != RecyclerView.NO_POSITION) {
                logProgressForUser(habitList.get(truePosition).getId(), 0,habit.getBestStreak());
                notifyItemChanged(truePosition);
            }
        });
    }

    private void checkForMilestone(Habit habit,List<Habit> habits) {
        SharedPreferences.Editor badgeEditor = badgePreferences.edit();
        int totalStreakCount = 0;
        int streakCount = habit.getStreakCount();
        int noOfHabits = habits.size();

        for(Habit hab : habits){
            totalStreakCount += hab.getStreakCount();
        }

        if(totalStreakCount >= 20 && !(badgePreferences.getBoolean("badge_logs_10", false))){
            soundPool.play(soundTotalHabitLog, 1, 1, 0, 0, 1);
            Toast.makeText(context, "Congrats! You have unlocked a new badge: Consistency Champ", Toast.LENGTH_SHORT).show();
            badgeEditor.putBoolean("badge_logs_10", true);
        }

        if(streakCount >= 10 && !(badgePreferences.getBoolean("badge_streak_7", false))){
            soundPool.play(soundConsistentHabitLog, 1, 1, 0, 0, 1);
            Toast.makeText(context, "Congrats! You have unlocked a new badge: 10 Log Streakster", Toast.LENGTH_SHORT).show();
            badgeEditor.putBoolean("badge_streak_7", true);
        }

        /*if(noOfHabits >= 5 && !badgePreferences.getBoolean("badge_habit_5", false)){
            Toast.makeText(context, "Congrats! You have unlocked a new badge: Habit Master", Toast.LENGTH_LONG).show();
            badgeEditor.putBoolean("badge_habit_5", true);
        }*/

        badgeEditor.apply();
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    private void deleteHabitFromFirestore(String habitId, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (position < 0 || position >= habitList.size()) {
            Toast.makeText(context, "Invalid item position", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .document(habitId)
                .delete()
                .addOnSuccessListener(unused -> {
                    // only for the development and testing part
                    if(habitList.isEmpty()){
                        Toast.makeText(context, "Preferences reset! Badges Lost", Toast.LENGTH_SHORT).show();
                        this.badgePreferences.edit().clear().apply();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditDialog(Habit habit, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Habit");

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_habit, null);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etGoal = view.findViewById(R.id.etEditGoal);

        etName.setText(habit.getName());
        etGoal.setText(habit.getGoal());

        builder.setView(view);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString();
            String newGoal = etGoal.getText().toString();

            updateHabitInFirestore(habit.getId(), newName, newGoal, position);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateHabitInFirestore(String id, String newName, String newGoal, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("goal", newGoal);

        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .document(id)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Habit updatedHabit = habitList.get(position);
                    updatedHabit.setName(newName);
                    updatedHabit.setGoal(newGoal);
                    notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update habit", Toast.LENGTH_SHORT).show();
                });
    }

    private void logProgressForUser(String habitId, int streakCount,int bestStreak){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("streakCount", streakCount);
        updates.put("bestStreak", bestStreak);

        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .document(habitId)
                .update(updates);
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName, tvStreak, tvHabitGoal,tvBestStreak;
        ImageView btnEdit,btnDelete,btnLog,btnResetStreak;
        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitGoal = itemView.findViewById(R.id.tvHabitGoal);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvStreak = itemView.findViewById(R.id.tvStreak);
            tvBestStreak = itemView.findViewById(R.id.tvBestStreak);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnLog = itemView.findViewById(R.id.btnLog);
            btnResetStreak = itemView.findViewById(R.id.btnResetStreak);
        }
    }
}
