package com.example.myapplication;

public class AttendanceRecord {
    private String name;
    private String roomNumber;
    private String readableTimestamp; // Changed from long to String
    private String deviceId;
    private String hostel;

    public AttendanceRecord() {
        // Default constructor required for Firestore deserialization
    }

    public AttendanceRecord(String name, String roomNumber, String hostel, String deviceId, String readableTimestamp) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.hostel = hostel;
        this.deviceId = deviceId;
        this.readableTimestamp = readableTimestamp;
    }

    public String getName() {
        return name;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getReadableTimestamp() {
        return readableTimestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getHostel() {
        return hostel;
    }

    // Optional setters if needed
}
