package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewAttendanceActivity extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();

    private FirebaseFirestore firestore;
    private static final String TAG = "ViewAttendanceActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        attendanceRecyclerView = findViewById(R.id.attendance_recycler_view);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceAdapter = new AttendanceAdapter(attendanceList);
        attendanceRecyclerView.setAdapter(attendanceAdapter);

        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            Log.d(TAG, "Logged in UID: " + currentUserId);
            loadTodayAttendance(currentUserId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTodayAttendance(String currentUserId) {
        String todayDate = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault()).format(new Date());
        String[] hostels = {"BH-1", "BH-2", "GH-1", "GH-2"};

        attendanceList.clear();

        for (String hostel : hostels) {
            firestore.collection("Attendance")
                    .document(todayDate)
                    .collection(hostel)
                    .document(currentUserId)  // Fetch directly using UID
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String nameField = doc.getString("name");
                            String roomNumber = doc.getString("roomNumber");
                            String deviceId = doc.getString("deviceId");
                            String readableTimestamp = doc.getString("readableTimestamp");

                            AttendanceRecord record = new AttendanceRecord(
                                    nameField, roomNumber, hostel, deviceId, readableTimestamp
                            );
                            attendanceList.add(record);
                            attendanceAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching data for " + hostel + ": " + e.getMessage()));
        }
    }
}
