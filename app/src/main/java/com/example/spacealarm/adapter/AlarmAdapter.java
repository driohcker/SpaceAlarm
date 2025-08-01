package com.example.spacealarm.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spacealarm.R;
import com.example.spacealarm.entity.Alarm;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private Context context;
    private List<Alarm> alarms;
    private OnAlarmClickListener clickListener;
    private OnAlarmToggleListener toggleListener;
    private OnLocationIconClickListener locationIconClickListener;

    public interface OnAlarmClickListener {
        void onAlarmClick(Alarm alarm);
    }

    public interface OnAlarmToggleListener {
        void onAlarmToggle(Alarm alarm, boolean isChecked);
    }

    public interface OnLocationIconClickListener {
        void onLocationIconClick(Alarm alarm);
    }

    public AlarmAdapter(Context context, OnAlarmClickListener clickListener, 
                        OnAlarmToggleListener toggleListener, OnLocationIconClickListener locationIconClickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.toggleListener = toggleListener;
        this.locationIconClickListener = locationIconClickListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm_card, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        if (alarms == null || position >= alarms.size()) return;

        Alarm alarm = alarms.get(position);
        holder.alarmTitle.setText(alarm.getTitle());
        holder.alarmAddress.setText(alarm.getAddress());
        holder.alarmRadius.setText(String.format("半径：%d米", (int) alarm.getRadius()));
        holder.alarmSwitch.setChecked(alarm.isEnabled());

        // 设置卡片点击事件
        holder.itemView.setOnClickListener(v -> clickListener.onAlarmClick(alarm));

        // 设置开关事件
        holder.alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleListener.onAlarmToggle(alarm, isChecked);
        });

        // 设置定位图标点击事件
        holder.locationIcon.setOnClickListener(v -> locationIconClickListener.onLocationIconClick(alarm));
    }

    @Override
    public int getItemCount() {
        return alarms == null ? 0 : alarms.size();
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmTitle;
        TextView alarmAddress;
        TextView alarmRadius;
        SwitchMaterial alarmSwitch;
        ImageView locationIcon;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmTitle = itemView.findViewById(R.id.alarmTitle);
            alarmAddress = itemView.findViewById(R.id.alarmAddress);
            alarmRadius = itemView.findViewById(R.id.alarmRadius);
            alarmSwitch = itemView.findViewById(R.id.alarmSwitch);
            locationIcon = itemView.findViewById(R.id.locationIcon);
        }
    }
}