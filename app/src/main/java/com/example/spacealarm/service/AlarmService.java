package com.example.spacealarm.service;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.mapper.AlarmMapper;
import com.example.spacealarm.mapper.impl.AlarmMapperImpl;

import java.util.List;

public class AlarmService {
    private static final String TAG = "AlarmService";
    private final AlarmMapper alarmMapper;
    private final Context context;

    public AlarmService(Context context) {
        this.context = context;
        this.alarmMapper = new AlarmMapperImpl(context);
    }

    // 创建新闹钟
    public long createAlarm(String title, double latitude, double longitude, float radius) {
        Alarm alarm = new Alarm(title, latitude, longitude, radius);
        return alarmMapper.insertAlarm(alarm);
    }

    // 创建带详细信息的闹钟
    public long createAlarmWithDetails(String title, double latitude, double longitude,
                                       float radius, String address, String message,
                                       boolean isVibrate, boolean isRing) {
        Alarm alarm = new Alarm(title, latitude, longitude, radius);
        alarm.setAddress(address);
        alarm.setMessage(message);
        alarm.setVibrate(isVibrate);
        alarm.setRing(isRing);
        return alarmMapper.insertAlarm(alarm);
    }

    // 获取所有闹钟
    public List<Alarm> getAllAlarms() {
        return alarmMapper.getAllAlarms();
    }

    // 获取启用的闹钟
    public List<Alarm> getEnabledAlarms() {
        return alarmMapper.getEnabledAlarms();
    }

    // 更新闹钟
    public boolean updateAlarm(Alarm alarm) {
        int result = alarmMapper.updateAlarm(alarm);
        return result > 0;
    }

    // 删除闹钟
    public boolean deleteAlarm(long alarmId) {
        int result = alarmMapper.deleteAlarm(alarmId);
        return result > 0;
    }

    // 根据ID获取闹钟
    public Alarm getAlarmById(long alarmId) {
        return alarmMapper.getAlarmById(alarmId);
    }

    // 切换闹钟启用状态
    public boolean toggleAlarmEnabled(long alarmId) {
        Alarm alarm = getAlarmById(alarmId);
        if (alarm != null) {
            alarm.setEnabled(!alarm.isEnabled());
            return updateAlarm(alarm);
        }
        return false;
    }

    // 检查位置是否在闹钟范围内
    public boolean isInAlarmRange(Alarm alarm, double currentLatitude, double currentLongitude) {
        if (!alarm.isEnabled()) {
            return false;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                alarm.getLatitude(), alarm.getLongitude(),
                currentLatitude, currentLongitude, results);

        float distance = results[0];
        return distance <= alarm.getRadius();
    }

    // 检查所有闹钟并返回触发的闹钟
    public Alarm checkAllAlarms(double currentLatitude, double currentLongitude) {
        List<Alarm> enabledAlarms = getEnabledAlarms();

        for (Alarm alarm : enabledAlarms) {
            if (isInAlarmRange(alarm, currentLatitude, currentLongitude)) {
                Log.d(TAG, "Alarm triggered: " + alarm.getTitle());
                return alarm;
            }
        }
        return null;
    }

    // 获取闹钟数量
    public int getAlarmCount() {
        return alarmMapper.getAlarmCount();
    }

    // 格式化距离显示
    public String formatDistance(float distance) {
        if (distance < 1000) {
            return String.format("%.0f米", distance);
        } else {
            return String.format("%.1f公里", distance / 1000);
        }
    }

    // 计算到闹钟的距离
    public float calculateDistanceToAlarm(Alarm alarm, double currentLatitude, double currentLongitude) {
        float[] results = new float[1];
        Location.distanceBetween(
                alarm.getLatitude(), alarm.getLongitude(),
                currentLatitude, currentLongitude, results);
        return results[0];
    }
}