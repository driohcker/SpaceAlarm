package com.example.spacealarm.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.spacealarm.service.NotificationService;

public class SettingsController {
    private static final String TAG = "SettingsController";
    private static final String PREF_NAME = "SpaceAlarmSettings";
    private static final String KEY_ALARM_ENABLED = "alarm_enabled";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_BACKGROUND_SERVICE_ENABLED = "background_service_enabled";
    // 新增：语音朗读开关
    private static final String KEY_TEXT_TO_SPEECH_ENABLED = "text_to_speech_enabled";

    private static SettingsController instance;
    private final SharedPreferences sharedPreferences;
    private SettingsViewCallback viewCallback;

    private SettingsController(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SettingsController getInstance(Context context) {
        if (null == instance) {
            instance = new SettingsController(context);
        }
        return instance;
    }

    public boolean isAlarmEnabled() {
        return sharedPreferences.getBoolean(KEY_ALARM_ENABLED, true);
    }

    public void setAlarmEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_ALARM_ENABLED, enabled).apply();
        if (null  != viewCallback) {
            viewCallback.onSettingsChanged();
        }
    }

    public boolean isVibrationEnabled() {
        return sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
        if (null  != viewCallback) {
            viewCallback.onSettingsChanged();
        }
    }

    public boolean isSoundEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
        if (null  != viewCallback) {
            viewCallback.onSettingsChanged();
        }
    }

    public boolean isBackgroundServiceEnabled() {
        return sharedPreferences.getBoolean(KEY_BACKGROUND_SERVICE_ENABLED, true);
    }

    public void setBackgroundServiceEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_BACKGROUND_SERVICE_ENABLED, enabled).apply();
        if (null  != viewCallback) {
            viewCallback.onSettingsChanged();
        }
    }

    // 新增：获取语音朗读是否启用
    public boolean isTextToSpeechEnabled() {
        return sharedPreferences.getBoolean(KEY_TEXT_TO_SPEECH_ENABLED, true);
    }

    // 新增：设置语音朗读是否启用
    public void setTextToSpeechEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_TEXT_TO_SPEECH_ENABLED, enabled).apply();
        if (null  != viewCallback) {
            viewCallback.onSettingsChanged();
        }
    }

    public interface SettingsViewCallback {
        void onSettingsChanged();
    }

    public void setViewCallback(SettingsViewCallback callback) {
        this.viewCallback = callback;
    }
}