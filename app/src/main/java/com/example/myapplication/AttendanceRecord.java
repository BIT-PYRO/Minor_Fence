package com.example.myapplication;

public class AttendanceRecord {
    private String name;
    private String roomNumber;
    private long timestamp;
    private String deviceId;
    private String hostel;

    public AttendanceRecord() {
        // Default constructor required for calls to DataSnapshot.getValue(AttendanceRecord.class)
    }

    public AttendanceRecord(String name, String roomNumber, String hostel, String deviceId, long timestamp) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.hostel = hostel;
        this.deviceId = deviceId;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getHostel() {
        return hostel;
    }

    // Optionally, add setters if needed
}
