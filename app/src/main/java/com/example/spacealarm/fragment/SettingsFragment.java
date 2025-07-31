package com.example.spacealarm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spacealarm.R;
import com.example.spacealarm.controller.SettingsController;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment implements SettingsController.SettingsViewCallback {
    private SwitchMaterial editAlarmEnabled;
    private SwitchMaterial editVibrationEnabled;
    private SwitchMaterial editSoundEnabled;
    private SettingsController settingsController;

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
    }

    @Override
    public void onSettingsChanged() {
        // 当设置更改时，更新UI
        loadSettings();
    }
}
