package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class WardenSignupActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button signupButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_signup);

        auth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        signupButton = findViewById(R.id.signup_button);
        progressBar = findViewById(R.id.progressBar);

        signupButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (task.isSuccessful()) {
                Toast.makeText(WardenSignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(WardenSignupActivity.this, WardenLoginActivity.class));
                finish();
            } else {
                Toast.makeText(WardenSignupActivity.this, "Signup failed! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}