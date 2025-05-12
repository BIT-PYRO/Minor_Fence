package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AttendanceMarkingActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText roomNumberInput;
    private AutoCompleteTextView hostelDropdown;
    private TextView userIdTextView, attendanceStatusTextView;
    private Button markAttendanceButton, logoutButton;
    private FirebaseFirestore firestore;
    private String userId, deviceId;

    private static final String TAG = "AttendanceMarking";

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_marking);

        hostelDropdown = findViewById(R.id.hostel_dropdown);
        roomNumberInput = findViewById(R.id.room_input);

        if (hostelDropdown == null) {
            Log.e(TAG, "hostelDropdown is NULL");
            return;
        }

        String[] hostels = {"BH-1", "BH-2", "GH-1", "GH-2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, hostels);
        hostelDropdown.setAdapter(adapter);

        // Initialize Firebase Auth & Firestore
        FirebaseAuth auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Log.d(TAG, "User not logged in! Redirecting to LoginActivity...");
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        } else {
            userId = user.getUid();
            Log.d(TAG, "User logged in with UID: " + userId);
        }

        // Get device ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        markAttendanceButton = findViewById(R.id.mark_attendance_button);
        logoutButton = findViewById(R.id.logout_button);
        userIdTextView = findViewById(R.id.user_id_textview);
        attendanceStatusTextView = findViewById(R.id.attendance_status_textview);

        userIdTextView.setText("User ID: " + userId);

        // Mark Attendance Button Click
        markAttendanceButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String hostel = hostelDropdown.getText().toString().trim();
            String roomNumber = roomNumberInput.getText().toString().trim();

            if (name.isEmpty() || hostel.isEmpty() || roomNumber.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                checkAndMarkAttendance(name, hostel, roomNumber);
            }
        });

        // Logout Button Click
        logoutButton.setOnClickListener(v -> logout());
    }

    // Check time before marking attendance
    private boolean isAttendanceAllowed() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        Log.d(TAG, "Current Time: " + hour + ":" + minute);

        return hour < 22 || (hour == 22 && minute <= 30);
    }

    private void checkAndMarkAttendance(String name, String hostel, String roomNumber) {
        if (!isAttendanceAllowed()) {
            Toast.makeText(this, "Attendance portal is closed! Try again tomorrow.", Toast.LENGTH_LONG).show();
            return;
        }

        String currentDate = getCurrentDate();
        DocumentReference attendanceRef = firestore.collection("Attendance")
                .document(currentDate)
                .collection(hostel)
                .document(userId);

        attendanceRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                attendanceStatusTextView.setText("Attendance Status: Already Marked");
                Toast.makeText(this, "Attendance already marked today!", Toast.LENGTH_SHORT).show();
            } else {
                markAttendance(name, hostel, roomNumber, currentDate);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error checking attendance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error fetching attendance record", e);
        });
    }

    private void markAttendance(String name, String hostel, String roomNumber, String date) {
        long timestamp = System.currentTimeMillis();
        AttendanceRecord attendanceRecord = new AttendanceRecord(name, roomNumber, hostel, deviceId, timestamp);

        firestore.collection("Attendance")
                .document(date)
                .collection(hostel)
                .document(userId)
                .set(attendanceRecord)
                .addOnSuccessListener(aVoid -> {
                    attendanceStatusTextView.setText("Attendance Status: Marked");
                    Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error marking attendance", e);
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User logged out. Redirecting to LoginActivity...");

        new Handler().postDelayed(this::navigateToLogin, 500);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AttendanceMarkingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
