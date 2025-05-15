package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class StudentDashboardActivity extends AppCompatActivity {

    private Button markAttendanceButton;
    private Button viewAttendanceButton;

    // Allowed attendance window: 10:00 PM to 10:30 PM
    private static final int ALLOWED_START_HOUR = 22; // 10:00 PM
    private static final int ALLOWED_START_MINUTE = 0;
    private static final int ALLOWED_END_HOUR = 22; // 10:30 PM
    private static final int ALLOWED_END_MINUTE = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize buttons
        markAttendanceButton = findViewById(R.id.mark_attendance_button);
        viewAttendanceButton = findViewById(R.id.view_attendance_button);

        // Handle attendance marking
        markAttendanceButton.setOnClickListener(v -> {
            if (isWithinAllowedTime()) {
                Intent intent = new Intent(StudentDashboardActivity.this, AttendanceMarkingActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(
                        this,
                        "Attendance can only be marked between 10:00 PM and 10:30 PM.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Handle viewing attendance history
        viewAttendanceButton.setOnClickListener(v -> {
            String attendanceUrl = "https://unifence-cc611.web.app/"; // Replace with your Firebase Hosting URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(attendanceUrl));
            startActivity(intent);
        });

    }

    // Time check method
    private boolean isWithinAllowedTime() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        int currentTimeInMinutes = (currentHour * 60) + currentMinute;
        int allowedStartTime = (ALLOWED_START_HOUR * 60) + ALLOWED_START_MINUTE;
        int allowedEndTime = (ALLOWED_END_HOUR * 60) + ALLOWED_END_MINUTE;

        return currentTimeInMinutes >= allowedStartTime && currentTimeInMinutes <= allowedEndTime;
    }
}
