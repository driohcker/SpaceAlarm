package com.example.spacealarm.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.spacealarm.activity.MainActivity;
import com.example.spacealarm.R;
import com.example.spacealarm.controller.SettingsController;
import com.example.spacealarm.entity.Alarm;

public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "space_alarm_channel"; // 固定 ID
    private static final String CHANNEL_NAME = "空间闹钟提醒";
    private static final int BASE_NOTIFICATION_ID = 1001;
    private int notificationCounter = 0; // 用于生成唯一的通知ID

    // 定义节奏型震动模式 - 震动两下
    private static final long[] VIBRATION_PATTERN = {0, 500, 300, 500};

    private static NotificationService instance;
    private final Context context;
    private final NotificationManager notificationManager;
    private final SettingsController settingsController;
    private final TextToSpeechManager textToSpeechManager;

    private Ringtone activeRingtone; // 当前正在播放的铃声

    // 私有构造函数，防止外部实例化
    private NotificationService(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.settingsController = SettingsController.getInstance(this.context);
        this.textToSpeechManager = TextToSpeechManager.getInstance(this.context);
        this.notificationCounter = 0;
        initNotificationService();
    }

    // 初始化 NotificationService
    private void initNotificationService() {
        Log.d(TAG, "初始化 NotificationService");
        createNotificationChannel();
        Log.d(TAG, "NotificationService 初始化完成");
    }

    // 获取单例实例
    public static synchronized NotificationService getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationService(context);
        }
        return instance;
    }

    // 创建固定通知渠道
    private void createNotificationChannel() {
        Log.d(TAG, "创建通知渠道开始");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null) {
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (existingChannel == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("空间闹钟提醒通知");
                channel.enableVibration(false); // 关闭系统震动，改用手动控制
                channel.setSound(null, null);   // 关闭系统声音，改用手动控制

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "通知渠道创建成功: " + CHANNEL_ID);
            } else {
                Log.d(TAG, "通知渠道已存在: " + CHANNEL_ID);
            }
        } else {
            Log.d(TAG, "设备版本低于 Android O，无需创建通知渠道");
        }
    }

    // 显示通知
    public void showAlarmNotification(Alarm alarm) {
        try {
            Log.d(TAG, "开始闹钟通知: " + alarm.getTitle());

            if (!settingsController.isAlarmEnabled()) {
                Log.d(TAG, "全局闹钟已禁用，不显示通知");
                return;
            }

            int notificationId = BASE_NOTIFICATION_ID + notificationCounter++;
            if (notificationCounter > 10000) {
                notificationCounter = 0;
            }

            boolean shouldVibrate = settingsController.isVibrationEnabled() && alarm.isVibrate();
            boolean shouldRing = settingsController.isSoundEnabled() && alarm.isRing();

            Log.d(TAG, "震动设置: 全局=" + settingsController.isVibrationEnabled() + ", 闹钟=" + alarm.isVibrate() + ", 最终=" + shouldVibrate);
            Log.d(TAG, "铃声设置: 全局=" + settingsController.isSoundEnabled() + ", 闹钟=" + alarm.isRing() + ", 最终=" + shouldRing);

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // 不设置声音和震动，只显示通知卡片
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("提醒: " + alarm.getTitle())
                    .setContentText(alarm.getAddress())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            if (notificationManager != null) {
                notificationManager.notify(notificationId, builder.build());
                Log.d(TAG, "通知成功发送，ID: " + notificationId);

                // 手动控制震动
                if (shouldVibrate) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1));
                        } else {
                            vibrator.vibrate(VIBRATION_PATTERN, -1);
                        }
                        Log.d(TAG, "震动：ON");
                    }
                } else {
                    Log.d(TAG, "震动：OFF");
                }

                // 手动控制响铃
                if (shouldRing) {
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    activeRingtone = RingtoneManager.getRingtone(context, alarmSound);
                    if (activeRingtone != null) {
                        activeRingtone.play();
                        Log.d(TAG, "铃声：ON");
                    }
                } else {
                    Log.d(TAG, "铃声：OFF");
                }

                // 语音朗读功能
                if (settingsController.isTextToSpeechEnabled()) {
                    Log.d(TAG, "语音：ON");
                    String message = "提醒: " + alarm.getTitle() + "，" + "位置: " + alarm.getAddress();
                    textToSpeechManager.speak(message);
                } else {
                    Log.d(TAG, "语音：OFF");
                }
            } else {
                Log.e(TAG, "通知管理器为空，无法发送通知");
            }

        } catch (Exception e) {
            Log.e(TAG, "显示通知时发生异常: " + e.getMessage(), e);
        }
    }

    // 停止当前铃声（可在需要时调用，比如关闭闹钟时）
    public void stopRingtone() {
        if (activeRingtone != null && activeRingtone.isPlaying()) {
            activeRingtone.stop();
            Log.d(TAG, "铃声已停止");
        }
    }
}
