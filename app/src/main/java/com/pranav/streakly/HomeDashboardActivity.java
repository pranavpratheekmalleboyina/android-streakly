package com.pranav.streakly;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.pranav.streakly.adapters.HabitAdapter;
import com.pranav.streakly.models.Habit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HomeDashboardActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    TextView tvGreeting, tvMotivation;
    FirebaseUser currentUser;
    FloatingActionButton fabAddHabit;
    List<Habit> habitList = new ArrayList<>();

    HabitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvMotivation = findViewById(R.id.tvMotivation);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fabAddHabit = findViewById(R.id.fabAddHabit);

        saveUserDetailsToFirestore();
        if(currentUser != null){
            String username = currentUser.getDisplayName();
            tvGreeting.setText("Hello " + username + " 👋");
        }else{
            tvGreeting.setText("Hello User 👋");
        }

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> setNavigationItems(item));

        setMotivationalQuote();

        fabAddHabit.setOnClickListener(this::addHabits);

        loadHabitsForUser();
    }

    // for loading the motivational qoute
    private void setMotivationalQuote() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Check if quote was already shown for this session
        String storedQuote = prefs.getString("session_quote", null);

        TextView tvMotivation = findViewById(R.id.tvMotivation);

        if (storedQuote != null) {
            // Display the same quote again
            tvMotivation.setText(storedQuote);
        } else {
            // First launch in this session — pick a new quote
            String[] quotes = getResources().getStringArray(R.array.motivational_quotes);
            String newQuote = quotes[new Random().nextInt(quotes.length)];

            // Save it for this session
            prefs.edit().putString("session_quote", newQuote).apply();

            tvMotivation.setText(newQuote);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadHabitsForUser(){
        RecyclerView recyclerHabits = findViewById(R.id.recyclerHabits);

        adapter = new HabitAdapter(habitList,this);

        recyclerHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerHabits.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("habits")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        // ✅ This is how you handle failure
                        Log.e("Firestore", "Listen failed", error);
                        Toast.makeText(this, "Failed to load habits", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    habitList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Habit habit = doc.toObject(Habit.class);
                        if(habit != null){habit.setId(doc.getId());}
                        habitList.add(habit);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private boolean setNavigationItems(MenuItem item){
        if(item.getItemId() == R.id.nav_home){
            return true;
        }else if(item.getItemId() == R.id.nav_stats){
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        }else if(item.getItemId() == R.id.nav_profile){
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return false;
    }

    private void addHabits(View view){
        Intent intent = new Intent(HomeDashboardActivity.this, AddHabitActivity.class);
        startActivity(intent);
    }

    private void saveUserDetailsToFirestore(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName(); // optional, can be null

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", name != null ? name : "Anonymous");
        userData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .set(userData, SetOptions.merge()) // merge keeps existing data (like habits)
                .addOnSuccessListener(unused -> Log.d("Firestore", "User saved/updated"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user", e));

    }
}
