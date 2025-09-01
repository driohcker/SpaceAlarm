package com.example.spacealarm.service.listener;

import com.example.spacealarm.entity.Alarm;

public interface LocationListener {
    void onLocationChanged(double latitude, double longitude, float accuracy, String address);
    void onLocationError(int errorCode);
}