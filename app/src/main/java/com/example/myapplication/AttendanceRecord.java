package com.example.myapplication;

public class AttendanceRecord {
    public String name;
    public String roomNumber;
    public long timestamp;
    public String deviceId;

    public AttendanceRecord(String name, String roomNumber, long timestamp, String deviceId) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.timestamp = timestamp;
        this.deviceId = deviceId;
    }
}
