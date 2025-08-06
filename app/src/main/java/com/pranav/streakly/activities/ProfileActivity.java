package com.pranav.streakly.activities;

import android.app.Activity;
import android.content.Context;
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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranav.streakly.R;
import com.pranav.streakly.base.NavigationActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import java.io.FileNotFoundException;
import java.io.InputStream;
import android.util.Log;

public class ProfileActivity extends NavigationActivity {

    // declaring all the ui elements
    boolean isEditing = false;
    EditText etUsername, etEmail;
    Button btnEdit, btnSave, btnLogout, btnChangeAvatar;
    ImageView ivAvatar; // for the profile picture
    FirebaseUser currentUser;
    SharedPreferences prefs;
    private ActivityResultLauncher<Intent> imagePickerLauncher; // for selecting the picture from the phone gallery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // for remembering the user data
        prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        // initializing all the elements
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        ivAvatar = findViewById(R.id.ivAvatar);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadUserDetails(currentUser);

        // the bottom navigation bar
        setupBottomNavigation(R.id.nav_profile);
        // Load saved data
        etUsername.setText(prefs.getString("username", "Guest"));
        etEmail.setText(prefs.getString("email", "guest@example.com"));
        String avatarUri = prefs.getString("avatarUri", null);
        if (avatarUri != null) Glide.with(this).load(Uri.parse(avatarUri)).into(ivAvatar);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        prefs.edit().putString("avatarUri", selectedImageUri.toString()).apply();
                        Glide.with(this).load(selectedImageUri).into(ivAvatar);// display
                        uploadAvatarToFirebase(currentUser,selectedImageUri); // 🔥 upload
                    }
                });

        // Edit button functionality
        btnEdit.setOnClickListener(v -> showConfirmDialog(
                "Edit Profile",
                "Are you sure you want to edit your profile?",
                "Yes",
                this::editDetails
        ));

        // Save button functionality
        btnSave.setOnClickListener(v -> showConfirmDialog(
                "Save Changes",
                "Are you sure you want to save your changes?",
                "Yes",
                this::saveDetails
        ));

        // Change avatar functionality
        btnChangeAvatar.setOnClickListener(v -> showConfirmDialog(
                "Change Avatar",
                "Are you sure you want to change your avatar?",
                "Yes",
                this::changeAvatar
        ));

        // Logout functionality
        btnLogout.setOnClickListener(v -> showConfirmDialog(
                "Logout",
                "Are you sure you want to logout?",
                "Yes",
                this::logoutUser
        ));
    }

    // uploading the selected photo to the firebase
    private void uploadAvatarToFirebase(FirebaseUser user,Uri imageUri){
        if (imageUri == null || user == null) {
            Toast.makeText(this, "Invalid image or user", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot access image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Storage path: avatars/{userId}.jpg
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference()
                    .child("avatars/" + user.getUid() + ".jpg");

            // Upload the image as a stream
            UploadTask uploadTask = storageRef.putStream(inputStream);
            uploadTask
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get download URL after upload
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();

                                    // Save the image URL to SharedPreferences
                                    prefs.edit().putString("avatarUri", downloadUrl).apply();

                                    // Optional: Save to Firebase User Profile (if needed)
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setPhotoUri(uri)
                                            .build();
                                    currentUser.updateProfile(profileUpdates);

                                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseStorage", "Upload failed" + e.getMessage(), e);
                        Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show();
                    });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Image does not exist at the location", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle avatar selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Glide.with(this).load(imageUri).into(ivAvatar);
            assert imageUri != null;
            prefs.edit().putString("avatarUri", imageUri.toString()).apply();
        }
    }

    // setting the different button states during the edit click
    private void editDetails(){
        isEditing = true;
        etUsername.setEnabled(true);
        btnSave.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.GONE);
        btnChangeAvatar.setVisibility(View.VISIBLE);
    }

    // saving the details to the db
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
                String fallbackName = email != null ? email.split("@")[0] : "user";
                etUsername.setText(fallbackName);
            }

            Glide.with(this).load(user.getPhotoUrl()).into(ivAvatar);

            // Save to SharedPreferences
            prefs.edit()
                    .putString("username", etUsername.getText().toString())
                    .putString("email", email)
                    .apply();
        }
    }

    // goes to the welcome screen after logging out
    private void logoutUser(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(ProfileActivity.this, WelcomeActivity.class));
        finish();
    }

    private void changeAvatar(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // the template of the confirm dialog
    private void showConfirmDialog(String title, String message, String positiveText, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, (dialog, which) -> onConfirm.run())
                .setNegativeButton("No", null)
                .show();
    }
}
