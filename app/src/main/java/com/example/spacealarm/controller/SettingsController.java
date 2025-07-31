package com.example.spacealarm.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SettingsController {
    private static final String TAG = "SettingsController";
    private static final String PREF_NAME = "SpaceAlarmSettings";
    private static final String KEY_ALARM_ENABLED = "alarm_enabled";
    private static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";

    private static SettingsController instance;
    private final SharedPreferences sharedPreferences;
    private SettingsViewCallback viewCallback;

    private SettingsController(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SettingsController getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsController(context);
        }
        return instance;
    }

    public boolean isAlarmEnabled() {
        return sharedPreferences.getBoolean(KEY_ALARM_ENABLED, true);
    }

    public void setAlarmEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_ALARM_ENABLED, enabled).apply();
        if (viewCallback != null) {
            viewCallback.onSettingsChanged();
        }
    }

    public boolean isVibrationEnabled() {
        return sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, true);
    }

    public void setVibrationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply();
        if (viewCallback != null) {
            viewCallback.onSettingsChanged();
        }
    }

    public boolean isSoundEnabled() {
        return sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true);
    }

    public void setSoundEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
        if (viewCallback != null) {
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