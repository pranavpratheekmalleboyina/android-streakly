package com.pranav.streakly;

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
    private EditText etHabitName, etTargetProgress;
    private Button btnSaveHabit;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        etHabitName = findViewById(R.id.etHabitName);
        etTargetProgress = findViewById(R.id.etTargetProgress);
        btnSaveHabit = findViewById(R.id.btnSaveHabit);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        btnSaveHabit.setOnClickListener(this::saveDetails);
    }

    private void saveDetails(View view){
        String name = etHabitName.getText().toString();
        String goal = etTargetProgress.getText().toString();

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
                    //Toast.makeText(this, "Habit saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // close screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save habit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
