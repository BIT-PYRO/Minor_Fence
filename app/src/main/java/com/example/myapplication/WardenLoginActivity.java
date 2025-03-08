package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;

public class WardenLoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton, signupButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_login);

        auth = FirebaseAuth.getInstance();

        // Initialize UI elements (with correct IDs)
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.go_to_signup_button); // Corrected ID
        progressBar = findViewById(R.id.progressBar); // Corrected ID

        if (loginButton != null) {
            loginButton.setOnClickListener(v -> loginUser());
        }
        if (signupButton != null) {
            signupButton.setOnClickListener(v ->
                    startActivity(new Intent(WardenLoginActivity.this, WardenSignupActivity.class))
            );
        }
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar and disable login button to prevent multiple clicks
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (loginButton != null) loginButton.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            // Hide progress bar and re-enable login button
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (loginButton != null) loginButton.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(WardenLoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(WardenLoginActivity.this, WardenDashboardActivity.class));
                finish(); // Close the login screen
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Toast.makeText(WardenLoginActivity.this, "Login failed! " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}