package com.pranav.streakly.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.pranav.streakly.R;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    // declaring all the ui elements
    private EditText txtName, txtEmailAddress, txtPassword, txtConfirmPassword;
    private Button btnRegister; // for registering the user
    private TextView loginLink; // link for the user to login in case they have already logged in
    private FirebaseAuth mAuth;  // for user authentication
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // initialize the firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // initialize the ui elements
        txtName = findViewById(R.id.txtName);
        txtEmailAddress = findViewById(R.id.txtEmailAddress);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        loginLink = findViewById(R.id.loginLink);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> registerUser());

        // navigates to login page in case the user already has an account
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    // method for registering the user
    private void registerUser() {
        String name = txtName.getText().toString().trim();
        String email = txtEmailAddress.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        String confirmPassword = txtConfirmPassword.getText().toString().trim();

        //name validation
        if (TextUtils.isEmpty(name)) {
            txtName.setError("Name is required");
            txtName.requestFocus();
            return;
        }

        // email validation
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmailAddress.setError("Valid email is required");
            txtEmailAddress.requestFocus();
            return;
        }

        // password validation
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            txtPassword.setError("Password must be at least 6 characters");
            txtPassword.requestFocus();
            return;
        }

        // password matching validation
        if (!password.equals(confirmPassword)) {
            txtConfirmPassword.setError("Passwords do not match");
            txtConfirmPassword.requestFocus();
            return;
        }

        // firebase registration of the user
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                assert firebaseUser != null;
                String uid = firebaseUser.getUid();

                FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .setValue(new User(name,email))
                        .addOnCompleteListener(dbTask -> {
                                   // saving the username to the firebase database
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)  // this is your 'name' variable
                                    .build();

                                    firebaseUser.updateProfile(profileUpdates);
                                showAlert("Success", "Registration successful!", true);
                        }).addOnFailureListener( e -> {
                                Log.e("FirebaseDB", "Error saving user", e);
                                showAlert("Error", "Failed to save user data!", false);
                        });
            } else {
                showAlert("Error", "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), false);
            }
        });
    }

    // Alert Dialog Function
    private void showAlert(String title, String message, boolean success) {
        new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (success) {
                        // Go to Login screen but only once after registering
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("from_register",true);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    // class for storing and retrieving the user data
    public static class User{
        public String name,email;

        public User(){}

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }

    }
}
