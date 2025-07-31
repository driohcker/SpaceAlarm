package com.example.spacealarm.entity;

import java.io.Serializable;

public class Alarm implements Serializable {
    private long id;
    private String title;
    private double latitude;
    private double longitude;
    private float radius; // 单位：米
    private String address;
    private String message;
    private boolean isVibrate;
    private boolean isRing;
    private boolean isEnabled;
    private long createdTime;

    public Alarm() {
        this.createdTime = System.currentTimeMillis();
        this.isEnabled = true;
        this.isVibrate = true;
        this.isRing = true;
        this.radius = 100; // 默认100米范围
    }

    public Alarm(String title, double latitude, double longitude, float radius) {
        this();
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isVibrate() { return isVibrate; }
    public void setVibrate(boolean vibrate) { isVibrate = vibrate; }

    public boolean isRing() { return isRing; }
    public void setRing(boolean ring) { isRing = ring; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", radius=" + radius +
                ", address='" + address + '\'' +
                ", message='" + message + '\'' +
                ", isVibrate=" + isVibrate +
                ", isRing=" + isRing +
                ", isEnabled=" + isEnabled +
                '}';
    }
}