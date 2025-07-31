package com.example.spacealarm.mapper;

import com.example.spacealarm.entity.Alarm;

import java.util.List;

public interface AlarmMapper {
    long insertAlarm(Alarm alarm);
    List<Alarm> getAllAlarms();
    List<Alarm> getEnabledAlarms();
    int updateAlarm(Alarm alarm);
    int deleteAlarm(long alarmId);
    Alarm getAlarmById(long alarmId);
    int getAlarmCount();
}