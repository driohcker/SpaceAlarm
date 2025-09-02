package com.example.spacealarm.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.spacealarm.activity.MainActivity;
import com.example.spacealarm.R;
import com.example.spacealarm.controller.SettingsController;
import com.example.spacealarm.entity.Alarm;

public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "space_alarm_channel";
    private static final String CHANNEL_NAME = "空间闹钟提醒";
    private static final int NOTIFICATION_ID = 1001;
    
    // 新增：定义节奏型震动模式 - 震动两下
    private static final long[] VIBRATION_PATTERN = {0, 500, 300, 500}; // 开始震动0ms，震动500ms，停300ms，再震动500ms

    private final Context context;
    private final NotificationManager notificationManager;
    private final SettingsController settingsController;
    private final TextToSpeechManager textToSpeechManager;

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.settingsController = SettingsController.getInstance(context);
        // 新增：初始化语音朗读管理器
        this.textToSpeechManager = TextToSpeechManager.getInstance(context);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        Log.d(TAG, "创建通知渠道开始");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("空间闹钟提醒通知");
            channel.enableVibration(true);
            // 修改震动模式，确保只震动两下
            channel.setVibrationPattern(VIBRATION_PATTERN);

            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "通知渠道创建成功: " + CHANNEL_ID);
        } else {
            Log.d(TAG, "设备版本低于Android O，无需创建通知渠道");
        }
    }

    public void showAlarmNotification(Alarm alarm) {
        try {
            Log.d(TAG, "显示闹钟通知开始: " + alarm.getTitle());

            // 检查全局闹钟是否启用
            if (!settingsController.isAlarmEnabled()) {
                Log.d(TAG, "全局闹钟已禁用，不显示通知");
                return;
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("提醒: " + alarm.getTitle())
                    .setContentText(alarm.getAddress())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true); // 修改为只提醒一次

            // 设置震动 (同时考虑全局设置和闹钟自身设置)
            boolean shouldVibrate = settingsController.isVibrationEnabled() && alarm.isVibrate();
            if (shouldVibrate) {
                builder.setVibrate(VIBRATION_PATTERN);
                Log.d(TAG, "通知设置震动（短震动两下）");
            }

            // 设置铃声 (同时考虑全局设置和闹钟自身设置)
            boolean shouldRing = settingsController.isSoundEnabled() && alarm.isRing();
            if (shouldRing) {
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                builder.setSound(alarmSound);
                Log.d(TAG, "通知设置铃声");
            }

            // 检查通知管理器是否为空
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
                Log.d(TAG, "通知成功发送，ID: " + NOTIFICATION_ID);

                // 新增：检查是否启用了语音朗读，如果启用则朗读通知内容
                if (settingsController.isTextToSpeechEnabled()) {
                    String message = "提醒: " + alarm.getTitle() + "，" + "位置: "+ alarm.getAddress();
                    textToSpeechManager.speak(message);
                }
            } else {
                Log.e(TAG, "通知管理器为空，无法发送通知");
            }

            // 额外触发设备震动
            if (shouldVibrate) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    vibrator.vibrate(VIBRATION_PATTERN, -1); // -1表示不重复震动
                    Log.d(TAG, "额外震动触发成功（短震动两下）");
                } else {
                    Log.e(TAG, "设备不支持震动");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "显示通知时发生异常: " + e.getMessage(), e);
        }
    }

    public void cancelNotification() {
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
            Log.d(TAG, "通知已取消");
        }
    }
}