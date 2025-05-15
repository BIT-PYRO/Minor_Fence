package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbsenteeListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ListView absenteeListView;
    private ArrayAdapter<String> adapter;
    private List<String> absenteeNames = new ArrayList<>();
    private String TAG = "AbsenteeListActivity";

    private String selectedDate;
    private String selectedHostel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absentee_list);

        absenteeListView = findViewById(R.id.absentee_list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, absenteeNames);
        absenteeListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        selectedDate = getIntent().getStringExtra("selectedDate");
        selectedHostel = getIntent().getStringExtra("selectedHostel");

        if (selectedDate != null && selectedHostel != null) {
            fetchPresentStudents();
        } else {
            Log.e(TAG, "Selected Date or Hostel is null!");
            Toast.makeText(this, "Invalid date or hostel", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchPresentStudents() {
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
                        absenteeNames.add("No absentees today!");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching users", e);
                    Toast.makeText(this, "Error fetching student list", Toast.LENGTH_SHORT).show();
                });
    }
}
