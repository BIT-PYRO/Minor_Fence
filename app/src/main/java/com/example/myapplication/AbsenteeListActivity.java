package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;

public class AbsenteeListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView absenteeListView;
    private ArrayAdapter<String> adapter;
    private List<String> absenteeNames = new ArrayList<>();
    private String TAG = "AbsenteeListActivity";

    private String selectedHostel;
    private Button selectDateButton;
    private TextView dateTextView;

    private Calendar today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absentee_list);

        absenteeListView = findViewById(R.id.absentee_list_view);
        selectDateButton = findViewById(R.id.select_date_button);
        dateTextView = findViewById(R.id.date_text_view);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, absenteeNames);
        absenteeListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        selectedHostel = getIntent().getStringExtra("selectedHostel");

        if (selectedHostel == null) {
            Toast.makeText(this, "Hostel not selected!", Toast.LENGTH_SHORT).show();
            finish();
        }

        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        selectDateButton.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar initial = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance();
                    picked.set(year, month, dayOfMonth, 0, 0, 0);
                    picked.set(Calendar.MILLISECOND, 0);

                    if (picked.after(today)) {
                        Toast.makeText(this, "Cannot select future dates!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(picked.getTime());
                    dateTextView.setText("Selected Date: " + formattedDate);
                    fetchPresentStudents(formattedDate);
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );

        // Set maximum date to today
        dialog.getDatePicker().setMaxDate(today.getTimeInMillis());
        dialog.show();
    }

    private void fetchPresentStudents(String selectedDate) {
        absenteeNames.clear();
        db.collection("attendance")
                .document(selectedDate)
                .collection(selectedHostel)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> presentUIDs = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        presentUIDs.add(doc.getId());
                    }
                    fetchAllStudentsFromUsers(presentUIDs);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching present students", e);
                    Toast.makeText(this, "Error fetching present students", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchAllStudentsFromUsers(Set<String> presentUIDs) {
        db.collection("users")
                .whereEqualTo("hostel", selectedHostel)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String uid = doc.getId();
                        String name = doc.getString("name");
                        if (name != null && !presentUIDs.contains(uid)) {
                            absenteeNames.add(name);
                        }
                    }

                    if (absenteeNames.isEmpty()) {
                        absenteeNames.add("No absentees on this day!");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching users", e);
                    Toast.makeText(this, "Error fetching student list", Toast.LENGTH_SHORT).show();
                });
    }
}