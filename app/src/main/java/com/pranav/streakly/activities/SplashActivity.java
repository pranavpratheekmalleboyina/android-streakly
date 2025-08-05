package com.pranav.streakly.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pranav.streakly.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    public static final int SPLASH_DURATION = 1000;
    private SharedPreferences prefs;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().remove("session_quote").apply();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //details about the animation
        ImageView splashLogo = findViewById(R.id.splashLogo);
        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
        fadeIn.setDuration(500); //the fade in occurs for only 0.5 second
        fadeIn.setFillAfter(true);
        splashLogo.startAnimation(fadeIn);

        //after the splash has occurred
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(currentUser != null){
                startActivity(new Intent(SplashActivity.this, HomeDashboardActivity.class));
            }else{
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            }
            finish();
        }, SPLASH_DURATION);
    }
}
