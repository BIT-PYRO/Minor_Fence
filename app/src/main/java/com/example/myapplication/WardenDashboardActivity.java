package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import android.app.TimePickerDialog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class WardenDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Button geofenceButton, viewAttendanceButton, setTimeButton, viewAbsenteesButton;
    private String selectedDate = "", selectedHostel = "";

    private final String[] hostelOptions = {"GH-1", "GH-2", "BH-1", "BH-2"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_dashboard);

        db = FirebaseFirestore.getInstance();

        geofenceButton = findViewById(R.id.geofence_button);
        viewAttendanceButton = findViewById(R.id.view_attendance_button);
        setTimeButton = findViewById(R.id.set_time_button);
        viewAbsenteesButton = findViewById(R.id.view_absentees_button);
        setTimeButton.setOnClickListener(v -> openTimePicker());

        geofenceButton.setOnClickListener(v -> {
            Intent intent = new Intent(WardenDashboardActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // 👇 This opens Firestore Attendance page directly
        viewAttendanceButton.setOnClickListener(v -> {
            // You can use static date or generate today’s date dynamically
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            String firebaseUrl = "https://console.firebase.google.com/u/0/project/unifence-cc611/firestore/databases/-default-/data/~2FAttendance~2F" + todayDate;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(firebaseUrl));
            startActivity(browserIntent);
        });

        viewAbsenteesButton.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    promptHostelSelection();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void promptHostelSelection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Hostel");

       // builder.setItems(hostelOptions, (dialog, which) -> {
        //    selectedHostel = hostelOptions[which];
         //   getAllStudents();
       // });
        builder.setItems(hostelOptions, (dialog, which) -> {
            selectedHostel = hostelOptions[which];

            // Build Firestore URL for absentees
            String firebaseAbsenteeUrl = "https://console.firebase.google.com/u/0/project/unifence-cc611/firestore/databases/-default-/data/~2Fabsentees~2F" + selectedDate + "~2F" + selectedHostel;

            // Open URL in browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(firebaseAbsenteeUrl));
            startActivity(browserIntent);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void getAllStudents() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, String> allStudents = new HashMap<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String uid = doc.getId();
                            String name = doc.getString("name");
                            String hostel = doc.getString("hostel");

                            if (hostel == null || hostel.equals(selectedHostel)) {
                                allStudents.put(uid, name);
                            }
                        }
                        getAttendanceData(allStudents);
                    } else {
                        Log.e("AbsenteeList", "Error fetching users", task.getException());
                        Toast.makeText(this, "Failed to fetch student list", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                WardenDashboardActivity.this,
                (view, selectedHour, selectedMinute) -> {

                    // Format time as HH:mm
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

                    // Ask for hostel before saving time
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Select Hostel for Attendance Time");

                    builder.setItems(hostelOptions, (dialog, which) -> {
                        String hostel = hostelOptions[which];

                        // Save to Firestore under 'settings' collection
                        db.collection("settings")
                                .document("attendance_time")
                                .update(hostel, formattedTime)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Attendance time set for " + hostel + ": " + formattedTime, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // If doc doesn't exist, create it
                                    Map<String, Object> newData = new HashMap<>();
                                    newData.put(hostel, formattedTime);
                                    db.collection("settings")
                                            .document("attendance_time")
                                            .set(newData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Attendance time set for " + hostel + ": " + formattedTime, Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(error -> {
                                                Toast.makeText(this, "Failed to set time: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.e("SetTime", "Error saving time", error);
                                            });
                                });
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.show();

                }, hour, minute, true
        );

        timePickerDialog.show();
    }

    private void getAttendanceData(Map<String, String> allStudents) {
        db.collection("attendance")
                .document(selectedDate)
                .collection(selectedHostel)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> presentUIDs = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            presentUIDs.add(doc.getId());
                        }

                        List<String> absentees = new ArrayList<>();
                        Map<String, String> absentMap = new HashMap<>();

                        for (String uid : allStudents.keySet()) {
                            if (!presentUIDs.contains(uid)) {
                                String name = allStudents.get(uid);
                                absentees.add(name);
                                absentMap.put(uid, name);
                            }
                        }

                        for (Map.Entry<String, String> entry : absentMap.entrySet()) {
                            db.collection("absentees")
                                    .document(selectedDate)
                                    .collection(selectedHostel)
                                    .document(entry.getKey())
                                    .set(Collections.singletonMap("name", entry.getValue()));
                        }

                        Intent intent = new Intent(WardenDashboardActivity.this, AbsenteeListActivity.class);
                        intent.putExtra("selectedDate", selectedDate);
                        intent.putExtra("selectedHostel", selectedHostel);
                        intent.putStringArrayListExtra("absenteeNames", new ArrayList<>(absentees));
                        startActivity(intent);

                    } else {
                        Log.e("AbsenteeList", "Error fetching attendance", task.getException());
                        Toast.makeText(this, "Failed to fetch attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
