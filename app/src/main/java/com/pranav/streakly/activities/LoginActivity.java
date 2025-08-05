package com.pranav.streakly.activities;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.pranav.streakly.R;

public class LoginActivity extends AppCompatActivity {

    // declaring all the ui elements
    private EditText txtEmailAddress, txtPassword;
    private Button loginBtn;  //for logging in the user
    private TextView registerLnk; // in case the user is not yet logged in
    private FirebaseAuth mAuth; // for authenticating the user and getting the user details

    // to login the user for the first time or after logging out
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize all the ui elements
        mAuth = FirebaseAuth.getInstance();
        txtEmailAddress = findViewById(R.id.txtEmailAddress);
        txtPassword = findViewById(R.id.txtPassword);
        registerLnk = findViewById(R.id.lnkRegister);
        loginBtn = findViewById(R.id.btlLogin);

        // triggers the logic when the user presses the login button
        loginBtn.setOnClickListener(v -> {
            loginUser();
        });
        // navigate to the register in case the user doesn't have an account yet
        registerLnk.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        });
    }

    // To direct an already logged-in user to the home dashboard
    @Override
    protected void onStart() {
        super.onStart();
        boolean fromRegister = getIntent().getBooleanExtra("from_register", false);
        if (mAuth.getCurrentUser() != null && !fromRegister) {
            startActivity(new Intent(LoginActivity.this, HomeDashboardActivity.class));
            finish();   // closes the login activity and prevents the user from going back
        }
    }

    // to authenticate the user before proceeding with the login
    private void loginUser(){
        String email = txtEmailAddress.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        // when the user leaves the email field empty
        if(TextUtils.isEmpty(email)){
            txtEmailAddress.setError("Please enter your email!!");
            return;
        }

        // when the user leaves the password field empty
        if(TextUtils.isEmpty(password)){
            txtPassword.setError("Please enter your password!!");
            return;
        }

        // when the user enters a password less than 6 characters long
        if(password.length() < 6){
            txtPassword.setError("Password must be atleast 6 characters long!!");
            return;
        }

        // setting the button to disabled status to prevent multiple login attempts
        loginBtn.setEnabled(false);

        // the process of logging in the user
        mAuth.signInWithEmailAndPassword(email,password).
                addOnCompleteListener(this, task -> {
                    loginBtn.setEnabled(true);
                    if(task.isSuccessful()){
                        Intent intent = new Intent(LoginActivity.this,HomeDashboardActivity.class);
                        startActivity(intent);
                        finish(); // closes the login activity and prevents the user from going back to it
                    }else{
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Authentication failed!";
                        Toast.makeText(LoginActivity.this,"Login failed!! Please Try again..",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
