package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Ensure IDs match those in XML
        Button studentButton = findViewById(R.id.student_button);
        Button wardenButton = findViewById(R.id.warden_button);

        if (studentButton == null || wardenButton == null) {
            Log.e("RoleSelectionActivity", "Button references are null. Check activity_role_selection.xml.");
            return;
        }

        studentButton.setOnClickListener(v -> {
            Log.d("RoleSelectionActivity", "Student button clicked. Navigating to LoginActivity.");
            Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        wardenButton.setOnClickListener(v -> {
            Log.d("RoleSelectionActivity", "Warden button clicked. Navigating to WardenLoginActivity.");
            Intent intent = new Intent(RoleSelectionActivity.this, WardenLoginActivity.class);
            startActivity(intent);
        });
    }
}
