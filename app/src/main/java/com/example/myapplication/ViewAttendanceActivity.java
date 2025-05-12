package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    private RecyclerView attendanceRecyclerView;
    private AttendanceAdapter attendanceAdapter;
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String currentStudentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        attendanceRecyclerView = findViewById(R.id.attendance_recycler_view);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentStudentName = currentUser.getDisplayName();
            loadAttendanceHistory();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAttendanceHistory() {
        firestore.collection("Attendance")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        attendanceList.clear();
                        for (QueryDocumentSnapshot dateDoc : task.getResult()) {
                            String date = dateDoc.getId();
                            String[] hostels = {"BH-1", "BH-2", "GH-1", "GH-2"};

                            for (String hostel : hostels) {
                                firestore.collection("Attendance")
                                        .document(date)
                                        .collection(hostel)
                                        .get()
                                        .addOnSuccessListener(snapshot -> {
                                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                                String name = doc.getString("name");

                                                if (name != null && name.equalsIgnoreCase(currentStudentName)) {
                                                    String roomNumber = doc.getString("roomNumber");
                                                    String deviceId = doc.getString("deviceId");
                                                    String h = doc.getString("hostel");
                                                    Long timestamp = doc.getLong("timestamp");

                                                    if (roomNumber != null && deviceId != null && h != null && timestamp != null) {
                                                        AttendanceRecord record = new AttendanceRecord(name, roomNumber, h, deviceId, timestamp);
                                                        attendanceList.add(record);

                                                        attendanceAdapter = new AttendanceAdapter(attendanceList);
                                                        attendanceRecyclerView.setAdapter(attendanceAdapter);
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(ViewAttendanceActivity.this, "Failed to load attendance records.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}