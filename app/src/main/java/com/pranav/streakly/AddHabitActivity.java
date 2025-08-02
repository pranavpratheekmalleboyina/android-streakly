package com.pranav.streakly;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class AddHabitActivity extends AppCompatActivity {
    private EditText etHabitName, etHabitGoal;
    private Button btnCreateHabit;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        etHabitName = findViewById(R.id.etHabitName);
        etHabitGoal = findViewById(R.id.etHabitGoal);
        btnCreateHabit = findViewById(R.id.btnCreateHabit);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        btnCreateHabit.setOnClickListener(
                v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Create Habit")
                            .setMessage("Are you sure you want to create this habit?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                createDetails();
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
        );
    }
    private void createDetails(){
        String name = etHabitName.getText().toString();
        String goal = etHabitGoal.getText().toString();

        if (name.isEmpty() || goal.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = (user != null) ? user.getUid() : "anonymous";

        Map<String, Object> habit = new HashMap<>();
        habit.put("name", name);
        habit.put("goal", goal);
        habit.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("habits")
                .add(habit)
                .addOnSuccessListener(docRef -> {
                    finish(); // close screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save habit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
