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
    
    // 新增：更新闹钟最后触发时间
    void updateAlarmLastTriggerTime(long alarmId, long triggerTime);
    
    // 新增：获取闹钟最后触发时间
    Long getAlarmLastTriggerTime(long alarmId);
}