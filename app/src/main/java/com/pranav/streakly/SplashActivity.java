package com.pranav.streakly;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    public static final int SPLASH_DURATION = 1000;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().remove("session_quote").apply();

        //details about the animation
        ImageView splashLogo = findViewById(R.id.splashLogo);
        AlphaAnimation fadeIn = new AlphaAnimation(0f,1f);
        fadeIn.setDuration(500); //the fade in occurs for only 0.5 second
        fadeIn.setFillAfter(true);
        splashLogo.startAnimation(fadeIn);

        //after the splash has occurred
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}
