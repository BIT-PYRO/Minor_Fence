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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class AttendanceMarkingActivity extends AppCompatActivity {

    private EditText nameInput, roomNumberInput;
    private AutoCompleteTextView hostelDropdown;
    private TextView userIdTextView, attendanceStatusTextView;
    private Button markAttendanceButton, logoutButton;

    private FirebaseFirestore firestore;
    private String userId, deviceId;
    private static final String TAG = "AttendanceMarking";

    private String currentDate;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isAttendanceAllowed()) {
            Toast.makeText(this, "Attendance portal is closed! Try again between 10:00 PM - 10:30 PM.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        firestore = FirebaseFirestore.getInstance();

        // Check if attendance has already been marked from this device
        firestore.collection("DeviceAttendance")
                .document(currentDate + "_" + deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(this, "This device has already marked attendance today.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        initializeUI(); // Only initialize UI if device hasn't marked attendance yet
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking attendance", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private boolean isAttendanceAllowed() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        return (hour == 22 && minute <= 30);
    }

    private void initializeUI() {
        setContentView(R.layout.activity_attendance_marking);

        nameInput = findViewById(R.id.name_input);
        roomNumberInput = findViewById(R.id.room_input);
        hostelDropdown = findViewById(R.id.hostel_dropdown);
        markAttendanceButton = findViewById(R.id.mark_attendance_button);
        logoutButton = findViewById(R.id.logout_button);
        userIdTextView = findViewById(R.id.user_id_textview);
        attendanceStatusTextView = findViewById(R.id.attendance_status_textview);

        String[] hostels = {"BH-1", "BH-2", "GH-1", "GH-2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, hostels);
        hostelDropdown.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        userId = user.getUid();
        userIdTextView.setText("User ID: " + userId);

        markAttendanceButton.setOnClickListener(v -> showBiometricPrompt());
        logoutButton.setOnClickListener(v -> logout());
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(), "Biometric verified. Please fill your details.", Toast.LENGTH_SHORT).show();
                        markAttendanceButton.setOnClickListener(v -> submitAttendance());
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Verify your identity to mark attendance")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void submitAttendance() {
        String name = nameInput.getText().toString().trim();
        String hostel = hostelDropdown.getText().toString().trim();
        String roomNumber = roomNumberInput.getText().toString().trim();

        if (name.isEmpty() || hostel.isEmpty() || roomNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all fields before submitting.", Toast.LENGTH_SHORT).show();
            return;
        }

        String readableTimestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

        AttendanceRecord record = new AttendanceRecord(name, roomNumber, hostel, deviceId, readableTimestamp);

        firestore.collection("Attendance")
                .document(currentDate)
                .collection(hostel)
                .document(userId)
                .set(record)
                .addOnSuccessListener(aVoid -> {
                    // Mark device usage
                    firestore.collection("DeviceAttendance")
                            .document(currentDate + "_" + deviceId)
                            .set(new DeviceUsage(userId))
                            .addOnSuccessListener(unused -> {
                                attendanceStatusTextView.setText("Attendance Status: Marked");
                                Toast.makeText(this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
                                markAttendanceButton.setEnabled(false);
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to mark attendance", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving attendance", e);
                });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(this::navigateToLogin, 500);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AttendanceMarkingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Helper class to store device usage info
    public static class DeviceUsage {
        public String userId;

        public DeviceUsage() {}

        public DeviceUsage(String userId) {
            this.userId = userId;
        }
    }
}
