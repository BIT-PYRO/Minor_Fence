package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceRecord> attendanceList;

    public AttendanceAdapter(List<AttendanceRecord> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_attendance_item, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = attendanceList.get(position);

        holder.nameText.setText(record.getName());
        holder.roomText.setText("Room: " + record.getRoomNumber());
        holder.hostelText.setText("Hostel: " + record.getHostel());
        holder.deviceText.setText("Device: " + record.getDeviceId());
        holder.timeText.setText("Time: " + record.getReadableTimestamp()); // updated line
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, roomText, hostelText, deviceText, timeText;

        public AttendanceViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            roomText = itemView.findViewById(R.id.room_text);
            hostelText = itemView.findViewById(R.id.hostel_text);
            deviceText = itemView.findViewById(R.id.device_text);
            timeText = itemView.findViewById(R.id.time_text);
        }
    }
}
