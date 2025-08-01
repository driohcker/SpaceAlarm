package com.example.spacealarm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.spacealarm.R;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.BaiduLocationService;

public class ForegroundLocationService extends Service {
    private static final String TAG = "ForegroundLocationService";
    private static final String CHANNEL_ID = "foreground_service_channel";
    private static final int NOTIFICATION_ID = 1002;

    private BaiduLocationService locationService;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务创建");

        // 初始化定位服务
        try {
            locationService = BaiduLocationService.getInstance(getApplicationContext());
            locationService.setOnLocationChangedListener(new BaiduLocationService.OnLocationChangedListener() {
                @Override
                public void onLocationChanged(double latitude, double longitude, float accuracy, String address) {
                    Log.d(TAG, "位置更新: " + latitude + ", " + longitude + ", 地址: " + address);
                    // 更新前台通知内容，使用地址信息
                    updateNotification("当前位置: " + address);
                }

                @Override
                public void onAlarmTriggered(Alarm alarm, double latitude, double longitude) {
                    Log.d(TAG, "闹钟触发: " + alarm.getTitle());
                    // 此处已由BaiduLocationService处理通知
                }

                @Override
                public void onLocationError(int errorCode) {
                    Log.e(TAG, "定位错误: " + errorCode);
                    updateNotification("定位错误: 错误码 " + errorCode);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "初始化定位服务失败: " + e.getMessage(), e);
        }

        // 初始化通知管理器
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 创建通知渠道
        createNotificationChannel();

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification("空间闹钟服务启动中..."));

        // 开始定位
        if (locationService != null) {
            locationService.startLocation();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "服务启动");
        return START_STICKY; // 确保服务被杀死后能重启
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不支持绑定
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "服务销毁");

        // 停止定位
        if (locationService != null) {
            locationService.stopLocation();
        }

        // 停止前台服务
        stopForeground(true);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "空间闹钟前台服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("用于后台持续定位和闹钟检测");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("空间闹钟服务")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // 设置为持续通知，不能被用户手动取消
                .build();
    }

    private void updateNotification(String content) {
        Notification notification = createNotification(content);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}