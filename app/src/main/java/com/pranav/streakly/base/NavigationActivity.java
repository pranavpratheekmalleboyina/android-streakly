package com.pranav.streakly.base;

import android.content.Intent;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pranav.streakly.activities.HomeDashboardActivity;
import com.pranav.streakly.activities.ProfileActivity;
import com.pranav.streakly.R;
import com.pranav.streakly.activities.StatsActivity;

public abstract class NavigationActivity extends AppCompatActivity {

    protected boolean shouldHandleBackToHome() {
        return !(this instanceof HomeDashboardActivity);
    }

    protected void setupBottomNavigation(int selectedItemId) {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(selectedItemId);  // highlight current item

        bottomNav.setOnItemSelectedListener(this::handleNavigation);
    }

    private boolean handleNavigation(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home && !(this instanceof HomeDashboardActivity)) {
            startActivity(new Intent(this, HomeDashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;

        } else if (itemId == R.id.nav_stats && !(this instanceof StatsActivity)) {
            startActivity(new Intent(this, StatsActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;

        } else if (itemId == R.id.nav_profile && !(this instanceof ProfileActivity)) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (shouldHandleBackToHome()) {
            Intent intent = new Intent(this, HomeDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();  // this will exit the app from Home
        }
    }

}
