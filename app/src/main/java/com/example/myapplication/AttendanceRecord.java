package com.example.myapplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceRecord {
    private String name;
    private String roomNumber;
    private String hostel;
    private String deviceId;
    private long timestamp;
    private String readableTimestamp;

    // Required empty constructor for Firestore
    public AttendanceRecord() {}

    // Constructor used in AttendanceMarkingActivity
    public AttendanceRecord(String name, String roomNumber, String hostel, String deviceId, String readableTimestamp) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.hostel = hostel;
        this.deviceId = deviceId;
        this.readableTimestamp = readableTimestamp;
        this.timestamp = System.currentTimeMillis(); // Optional: store the epoch timestamp
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getHostel() { return hostel; }
    public void setHostel(String hostel) { this.hostel = hostel; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getReadableTimestamp() { return readableTimestamp; }
    public void setReadableTimestamp(String readableTimestamp) { this.readableTimestamp = readableTimestamp; }
}
