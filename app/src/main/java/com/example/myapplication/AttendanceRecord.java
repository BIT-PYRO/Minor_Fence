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

    public AttendanceRecord(String name, String roomNumber, long timestamp, String deviceId, String hostel) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
        this.hostel = hostel;
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
