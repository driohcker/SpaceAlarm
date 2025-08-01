package com.example.spacealarm.controller;

import android.content.Context;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.AlarmService;

import java.util.List;

public class AlarmController {
    private final AlarmService alarmService;
    private final Context context;
    private MainViewCallback viewCallback;

    public interface MainViewCallback {
        void onAlarmsLoaded(List<Alarm> alarms);
        void onAlarmDeleted(long alarmId);
        void onAlarmToggled(long alarmId, boolean enabled);
        void onError(String error);
    }

    public AlarmController(Context context) {
        this.context = context;
        this.alarmService = new AlarmService(context);
    }

    public void setViewCallback(MainViewCallback callback) {
        this.viewCallback = callback;
    }

    public void loadAllAlarms() {
        try {
            List<Alarm> alarms = alarmService.getAllAlarms();
            if (viewCallback != null) {
                viewCallback.onAlarmsLoaded(alarms);
            }
        } catch (Exception e) {
            if (viewCallback != null) {
                viewCallback.onError("加载闹钟失败: " + e.getMessage());
            }
        }
    }

    public void loadEnabledAlarms() {
        try {
            List<Alarm> alarms = alarmService.getEnabledAlarms();
            if (viewCallback != null) {
                viewCallback.onAlarmsLoaded(alarms);
            }
        } catch (Exception e) {
            if (viewCallback != null) {
                viewCallback.onError("加载启用闹钟失败: " + e.getMessage());
            }
        }
    }

    public void deleteAlarm(long alarmId) {
        try {
            boolean success = alarmService.deleteAlarm(alarmId);
            if (viewCallback != null) {
                if (success) {
                    viewCallback.onAlarmDeleted(alarmId);
                } else {
                    viewCallback.onError("删除闹钟失败");
                }
            }
        } catch (Exception e) {
            if (viewCallback != null) {
                viewCallback.onError("删除闹钟失败: " + e.getMessage());
            }
        }
    }

    public void toggleAlarm(long alarmId) {
        try {
            boolean success = alarmService.toggleAlarmEnabled(alarmId);
            if (viewCallback != null) {
                if (success) {
                    Alarm alarm = alarmService.getAlarmById(alarmId);
                    viewCallback.onAlarmToggled(alarmId, alarm != null && alarm.isEnabled());
                } else {
                    viewCallback.onError("切换闹钟状态失败");
                }
            }
        } catch (Exception e) {
            if (viewCallback != null) {
                viewCallback.onError("切换闹钟状态失败: " + e.getMessage());
            }
        }
    }

    public Alarm getAlarmDetails(long alarmId) {
        return alarmService.getAlarmById(alarmId);
    }

    public int getAlarmCount() {
        return alarmService.getAlarmCount();
    }

    public boolean updateAlarm(Alarm alarm) {
        try {
            return alarmService.updateAlarm(alarm);
        } catch (Exception e) {
            if (viewCallback != null) {
                viewCallback.onError("更新闹钟失败: " + e.getMessage());
            }
            return false;
        }
    }

    public void refreshAlarms() {
        loadAllAlarms();
    }
}