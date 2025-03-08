package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class WardenDashboardActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    private Button geofenceButton, viewAttendanceButton, setTimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_dashboard);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        geofenceButton = findViewById(R.id.geofence_button);
        viewAttendanceButton = findViewById(R.id.view_attendance_button);
        setTimeButton = findViewById(R.id.set_time_button);

        // Navigate to MapsActivity for placing geofences
        geofenceButton.setOnClickListener(v -> {
            Intent intent = new Intent(WardenDashboardActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // Navigate to AttendanceRecordsActivity for viewing student records
//        viewAttendanceButton.setOnClickListener(v -> {
//            Intent intent = new Intent(WardenDashboardActivity.this, AttendanceRecordsActivity.class);
//            startActivity(intent);
//        });
//
//        // Navigate to SetAttendanceTimeActivity for dynamic time setting
//        setTimeButton.setOnClickListener(v -> {
//            Intent intent = new Intent(WardenDashboardActivity.this, SetAttendanceTimeActivity.class);
//            startActivity(intent);
//        });
    }
}
