package com.example.accelerometerdemo;

public class AccelerometerDataPoint {
    public int timestamp;
    public float x, y, z;

    public AccelerometerDataPoint(int timestamp, float x, float y, float z) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
