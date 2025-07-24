package com.pranav.streakly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Random;

public class HomeDashboardActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    TextView tvGreeting, tvMotivation;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_dashboard);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvMotivation = findViewById(R.id.tvMotivation);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null){
            String username = currentUser.getDisplayName();
            tvGreeting.setText("Hello " + username + " 👋");
        }else{
            tvGreeting.setText("Hello User 👋");
        }

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
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
        });
        setMotivationalQuote();
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

}
