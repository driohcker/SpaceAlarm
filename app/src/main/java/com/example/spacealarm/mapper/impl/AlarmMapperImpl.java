package com.example.spacealarm.mapper.impl;

import android.content.Context;

import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.mapper.AlarmMapper;
import com.example.spacealarm.mapper.helper.AlarmDatabaseHelper;

import java.util.List;

public class AlarmMapperImpl implements AlarmMapper {
    private final AlarmDatabaseHelper dbHelper;

    public AlarmMapperImpl(Context context) {
        this.dbHelper = new AlarmDatabaseHelper(context);
    }

    @Override
    public long insertAlarm(Alarm alarm) {
        return dbHelper.insertAlarm(alarm);
    }

    @Override
    public List<Alarm> getAllAlarms() {
        return dbHelper.getAllAlarms();
    }

    @Override
    public List<Alarm> getEnabledAlarms() {
        return dbHelper.getEnabledAlarms();
    }

    @Override
    public int updateAlarm(Alarm alarm) {
        return dbHelper.updateAlarm(alarm);
    }

    @Override
    public int deleteAlarm(long alarmId) {
        return dbHelper.deleteAlarm(alarmId);
    }

    @Override
    public Alarm getAlarmById(long alarmId) {
        return dbHelper.getAlarmById(alarmId);
    }

    @Override
    public int getAlarmCount() {
        return dbHelper.getAlarmCount();
    }
}