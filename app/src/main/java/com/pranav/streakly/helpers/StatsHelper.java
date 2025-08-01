package com.pranav.streakly.helpers;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pranav.streakly.models.Habit;

import java.util.ArrayList;
import java.util.List;

public class StatsHelper {

    public interface StatsCallback {
        void onStatsComputed(int totalHabits, int totalLogs, int currentStreak, List<Habit> habitList);
        void onFailure(Exception e);
    }

    public static void computeStats(Context context, StatsCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("habits")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Habit> habits = new ArrayList<>();
                    int totalLogs = 0;
                    int bestStreak = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Habit habit = doc.toObject(Habit.class);
                        habits.add(habit);

                        totalLogs += habit.getStreakCount();
                        if (habit.getBestStreak() > bestStreak) {
                            bestStreak = habit.getBestStreak();
                        }
                    }

                    callback.onStatsComputed(habits.size(), totalLogs, bestStreak, habits);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
