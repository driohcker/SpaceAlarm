package com.example.spacealarm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.spacealarm.service.ForegroundLocationService;

import com.example.spacealarm.R;
import com.example.spacealarm.controller.SettingsController;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment implements SettingsController.SettingsViewCallback {
    private SwitchMaterial editAlarmEnabled;
    private SwitchMaterial editVibrationEnabled;
    private SwitchMaterial editSoundEnabled;
    private SwitchMaterial editBackgroundServiceEnabled;
    // 新增：语音朗读开关
    private SwitchMaterial editTextToSpeechEnabled;
    private SettingsController settingsController;
    
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        settingsController = SettingsController.getInstance(getActivity());
        settingsController.setViewCallback(this);

        // 初始化开关控件
        editAlarmEnabled = view.findViewById(R.id.editAlarmEnabled);
        editVibrationEnabled = view.findViewById(R.id.editVibrationEnabled);
        editSoundEnabled = view.findViewById(R.id.editSoundEnabled);
        editBackgroundServiceEnabled = view.findViewById(R.id.editBackgroundServiceEnabled);
        // 新增：初始化语音朗读开关
        editTextToSpeechEnabled = view.findViewById(R.id.editTextToSpeechEnabled);

        // 加载设置
        loadSettings();

        // 设置开关监听器
        setupSwitchListeners();

        return view;
    }

    private void loadSettings() {
        editAlarmEnabled.setChecked(settingsController.isAlarmEnabled());
        editVibrationEnabled.setChecked(settingsController.isVibrationEnabled());
        editSoundEnabled.setChecked(settingsController.isSoundEnabled());
        editBackgroundServiceEnabled.setChecked(settingsController.isBackgroundServiceEnabled());
        // 新增：加载语音朗读设置
        editTextToSpeechEnabled.setChecked(settingsController.isTextToSpeechEnabled());
    }

    private void setupSwitchListeners() {
        editAlarmEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsController.setAlarmEnabled(isChecked);
            }
        });

        editVibrationEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsController.setVibrationEnabled(isChecked);
            }
        });

        editSoundEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsController.setSoundEnabled(isChecked);
            }
        });
        
        editBackgroundServiceEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsController.setBackgroundServiceEnabled(isChecked);
                // 根据设置状态控制后台服务
                Intent serviceIntent = new Intent(mContext, ForegroundLocationService.class);
                if (isChecked) {
                    // 启动服务
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mContext.startForegroundService(serviceIntent);
                    } else {
                        mContext.startService(serviceIntent);
                    }
                } else {
                    // 停止服务
                    mContext.stopService(serviceIntent);
                }
            }
        });
        // 新增：语音朗读开关监听器
        editTextToSpeechEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingsController.setTextToSpeechEnabled(isChecked);
            }
        });
    }

    @Override
    public void onSettingsChanged() {
        // 当设置更改时，更新UI
        loadSettings();
    }
}

