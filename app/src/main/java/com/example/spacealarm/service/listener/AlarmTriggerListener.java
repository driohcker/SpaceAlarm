package com.example.spacealarm.service.listener;

import com.example.spacealarm.entity.Alarm;

public interface AlarmTriggerListener {
    void onAlarmTriggered(Alarm alarm, double latitude, double longitude);
}