package com.example.spacealarm.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spacealarm.R;
import com.example.spacealarm.adapter.AlarmAdapter;
import com.example.spacealarm.controller.MainController;
import com.example.spacealarm.entity.Alarm;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AlarmFragment extends Fragment implements MainController.MainViewCallback {
    private MainController mainController;
    private RecyclerView recyclerView;
    private AlarmAdapter alarmAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);

        // 初始化视图和控制器
        recyclerView = view.findViewById(R.id.alarmRecyclerView);
        mainController = new MainController(getActivity());
        mainController.setViewCallback(this);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        alarmAdapter = new AlarmAdapter(getActivity(), this::onAlarmClick, this::onAlarmToggle);
        recyclerView.setAdapter(alarmAdapter);

        // 加载闹钟数据
        mainController.loadAllAlarms();
        return view;
    }

    private void onAlarmClick(Alarm alarm) {
        // 显示编辑闹钟对话框
        showEditAlarmDialog(alarm);
    }

    private void onAlarmToggle(Alarm alarm, boolean isChecked) {
        alarm.setEnabled(isChecked);
        mainController.toggleAlarm(alarm.getId());
    }

    @Override
    public void onAlarmsLoaded(List<Alarm> alarms) {
        if (isAdded() && alarmAdapter != null) {
            alarmAdapter.setAlarms(alarms);
            alarmAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAlarmDeleted(long alarmId) {
        mainController.loadAllAlarms();
    }

    @Override
    public void onAlarmToggled(long alarmId, boolean enabled) {
        mainController.loadAllAlarms();
    }

    @Override
    public void onError(String error) {
        if (isAdded()) {
            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mainController != null) {
            mainController.loadAllAlarms();
        }
    }

    // 显示编辑闹钟对话框
    private void showEditAlarmDialog(final Alarm alarm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_alarm, null);
        builder.setView(dialogView);

        // 初始化对话框控件
        EditText editAlarmName = dialogView.findViewById(R.id.editAlarmName);
        Slider editAlarmRadius = dialogView.findViewById(R.id.editAlarmRadius);
        TextView radiusValue = dialogView.findViewById(R.id.radiusValue);
        SwitchMaterial switchVibration = dialogView.findViewById(R.id.switchVibration);
        SwitchMaterial switchSound = dialogView.findViewById(R.id.switchSound);
        
        // 填充现有闹钟信息
        editAlarmName.setText(alarm.getTitle());
        editAlarmRadius.setValue(alarm.getRadius());
        radiusValue.setText((int)alarm.getRadius() + "米");
        switchVibration.setChecked(alarm.isVibrate());
        switchSound.setChecked(alarm.isRing());
        
        // 更新半径显示
        editAlarmRadius.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                radiusValue.setText((int)value + "米");
            }
        });
        
        builder.setTitle("编辑空间闹钟")
            .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // 获取输入值
                    String title = editAlarmName.getText().toString().trim();
                    float radius = editAlarmRadius.getValue();
                    boolean isVibrate = switchVibration.isChecked();
                    boolean isRing = switchSound.isChecked();
                    
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "请输入闹钟名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // 更新闹钟信息
                    alarm.setTitle(title);
                    alarm.setRadius(radius);
                    alarm.setVibrate(isVibrate);
                    alarm.setRing(isRing);
                    
                    boolean success = mainController.updateAlarm(alarm);
                    
                    if (success) {
                        Toast.makeText(getContext(), "闹钟更新成功", Toast.LENGTH_SHORT).show();
                        mainController.loadAllAlarms();
                    } else {
                        Toast.makeText(getContext(), "闹钟更新失败", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            })
            // 添加删除按钮
            .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 显示确认删除对话框
                    new AlertDialog.Builder(getActivity())
                        .setTitle("确认删除")
                        .setMessage("确定要删除这个闹钟吗？")
                        .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mainController.deleteAlarm(alarm.getId());
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                }
            });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
