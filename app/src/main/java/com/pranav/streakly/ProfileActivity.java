package com.pranav.streakly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends NavigationActivity {
    boolean isEditing = false;
    EditText etUsername, etEmail;
    Button btnEdit, btnSave, btnLogout, btnChangeAvatar;
    ImageView ivAvatar;
    FirebaseUser currentUser;
    SharedPreferences prefs;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        ivAvatar = findViewById(R.id.ivAvatar);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        loadUserDetails(currentUser);

        setupBottomNavigation(R.id.nav_profile);
        // Load saved data
        etUsername.setText(prefs.getString("username", "Guest"));
        etEmail.setText(prefs.getString("email", "guest@example.com"));
        String avatarUri = prefs.getString("avatarUri", null);
        //if (avatarUri != null) ivAvatar.setImageURI(Uri.parse(avatarUri));
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        ivAvatar.setImageURI(selectedImageUri);
                        prefs.edit().putString("avatarUri", selectedImageUri.toString()).apply();
                    }
                });

        // Edit button
        btnEdit.setOnClickListener(v -> showConfirmDialog(
                "Edit Profile",
                "Are you sure you want to edit your profile?",
                "Yes",
                this::editDetails
        ));

        // Save button
        btnSave.setOnClickListener(v -> showConfirmDialog(
                "Save Changes",
                "Are you sure you want to save your changes?",
                "Yes",
                this::saveDetails
        ));

        // Change avatar
        btnChangeAvatar.setOnClickListener(v -> showConfirmDialog(
                "Change Avatar",
                "Are you sure you want to change your avatar?",
                "Yes",
                this::changeAvatar
        ));

        // Logout
        btnLogout.setOnClickListener(v -> showConfirmDialog(
                "Logout",
                "Are you sure you want to logout?",
                "Yes",
                this::logoutUser
        ));
    }

    // Handle avatar selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivAvatar.setImageURI(imageUri);
            assert imageUri != null;
            prefs.edit().putString("avatarUri", imageUri.toString()).apply();
        }
    }

    private void editDetails(){
        isEditing = true;
        etUsername.setEnabled(true);
        btnSave.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.GONE);
        btnChangeAvatar.setVisibility(View.VISIBLE);
    }

    private void saveDetails(){
        String updatedName = etUsername.getText().toString();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                      .setDisplayName(updatedName).build();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){currentUser.updateProfile(profileUpdates);}
        prefs.edit().putString("username", updatedName).apply();
        etUsername.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        btnEdit.setVisibility(View.VISIBLE);
        btnChangeAvatar.setVisibility(View.GONE);
        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
    }

    // to load the user details from firebase to the profile settings
    private void loadUserDetails(FirebaseUser user){
        if (user != null) {
            // Get email
            String email = user.getEmail();
            etEmail.setText(email);

            // Get display name (nullable)
            String displayName = user.getDisplayName();

            if (displayName != null && !displayName.isEmpty()) {
                etUsername.setText(displayName);
            } else {
                // Fallback: use email username
                String fallbackName = email != null ? email.split("@")[0] : "User";
                etUsername.setText(fallbackName);
            }

            // Save to SharedPreferences
            prefs.edit()
                    .putString("username", etUsername.getText().toString())
                    .putString("email", email)
                    .apply();
        }
    }

    private void logoutUser(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ProfileActivity.this, WelcomeActivity.class));
        finish();
    }

    private void changeAvatar(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void showConfirmDialog(String title, String message, String positiveText, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }

}
