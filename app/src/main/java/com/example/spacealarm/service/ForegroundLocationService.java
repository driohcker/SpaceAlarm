package com.example.spacealarm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.spacealarm.AppApplication;
import com.example.spacealarm.R;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.BaiduLocationService;
import com.example.spacealarm.service.listener.AlarmTriggerListener;
import com.example.spacealarm.service.listener.LocationListener;

// 添加新的导入
import android.os.Process;

public class ForegroundLocationService extends Service {
    private static final String TAG = "ForegroundLocationService";
    private static final String CHANNEL_ID = "foreground_service_channel";
    private static final int NOTIFICATION_ID = 1002;
    // 添加关闭按钮的请求代码
    private static final int CLOSE_APP_REQUEST_CODE = 1003;
    private static final String ACTION_CLOSE_APP = "com.example.spacealarm.ACTION_CLOSE_APP";

    private BaiduLocationService locationService;
    private NotificationManager notificationManager;

    // 内部定位监听器实现
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(double latitude, double longitude, float accuracy, String address) {
            Log.d(TAG, "位置更新: " + latitude + ", " + longitude + ", 地址: " + address);
            // 更新前台通知内容，使用地址信息
            updateNotification("当前位置: " + address);
        }

        @Override
        public void onLocationError(int errorCode) {
            Log.e(TAG, "定位错误: " + errorCode);
            updateNotification("定位错误: 错误码 " + errorCode);
        }
    };

    // 内部闹钟触发监听器实现
    private final AlarmTriggerListener alarmTriggerListener = new AlarmTriggerListener() {
        @Override
        public void onAlarmTriggered(Alarm alarm, double latitude, double longitude) {
            Log.d(TAG, "闹钟触发: " + alarm.getTitle());
            // 此处已由BaiduLocationService处理通知
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "后台服务创建");

        // 初始化定位服务
        try {
            locationService = BaiduLocationService.getInstance(getApplicationContext());
            // 注册监听器
            locationService.addLocationListener(locationListener);
            locationService.addAlarmTriggerListener(alarmTriggerListener);
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
        // 检查是否是关闭应用的意图
        if (intent != null && ACTION_CLOSE_APP.equals(intent.getAction())) {
            Log.d(TAG, "收到关闭应用的请求");
            // 完全关闭应用
            forceCloseApp();
            return START_NOT_STICKY;
        }
        
        // 增加权限检查，确保只有在权限已授权的情况下才继续执行
        if (!AppApplication.isPermissionsGranted) {
            Log.w(TAG, "服务启动时权限未授权，正在等待授权...");
            // 返回START_NOT_STICKY，避免系统反复重启未授权的服务
            return START_NOT_STICKY;
        }
        
        // 原有的初始化代码
        try {
            // 初始化定位服务
            if (locationService == null) {
                locationService = BaiduLocationService.getInstance(this);
            }
            
            // 确保定位服务在每次服务启动时都重新启动
            if (locationService != null) {
                locationService.restartLocation();
            }
            return START_STICKY; // 确保服务被杀死后能重启
        } catch (Exception e) {
            Log.e(TAG, "服务启动异常: " + e.getMessage());
        }
        
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不支持绑定
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "后台服务销毁");

        // 移除监听器
        if (locationService != null) {
            locationService.removeLocationListener(locationListener);
            locationService.removeAlarmTriggerListener(alarmTriggerListener);
            locationService.stopLocation();
        }

        // 停止前台服务
        stopForeground(true);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "任务被移除，重新启动服务");
        // 当用户从最近任务列表中滑动移除应用时，重新启动服务
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartServiceIntent);
        } else {
            startService(restartServiceIntent);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "空间闹钟前台服务",
                    NotificationManager.IMPORTANCE_HIGH  // 提高重要性级别，确保通知能正常显示操作按钮
            );
            channel.setDescription("用于后台持续定位和闹钟检测");
            channel.setShowBadge(true);  // 显示角标
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);  // 锁屏显示
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification(String content) {
        // 创建关闭应用的Intent
        Intent closeAppIntent = new Intent(this, ForegroundLocationService.class);
        closeAppIntent.setAction(ACTION_CLOSE_APP);
        
        // 创建PendingIntent
        PendingIntent closeAppPendingIntent = PendingIntent.getService(
                this, 
                CLOSE_APP_REQUEST_CODE, 
                closeAppIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        // 创建启动应用的Intent（点击通知本身时触发）
        Intent appIntent = new Intent(this, com.example.spacealarm.activity.MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent appPendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                appIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        // 使用Builder创建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("空间闹钟后台服务进行中")
                .setContentText(content)
                .setContentIntent(appPendingIntent)  // 设置点击通知的意图
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // 高优先级
                .setOngoing(true)  // 设置为持续通知
                .setAutoCancel(false)  // 不自动取消
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true);  // 仅提醒一次
        
        // 使用BigTextStyle来确保通知展开时有足够的空间显示按钮
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.bigText(content + "\n（此通知未关闭即为后台服务进行中）");
        builder.setStyle(bigTextStyle);
        
        // 添加关闭应用按钮
        builder.addAction(R.drawable.ic_close, "关闭应用", closeAppPendingIntent);
        
        return builder.build();
    }

    private void updateNotification(String content) {
        Notification notification = createNotification(content);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
    // 添加一个新方法用于完全关闭应用
    private void forceCloseApp() {
        Log.d(TAG, "正在强制关闭应用...");
        
        // 停止定位服务
        if (locationService != null) {
            locationService.removeLocationListener(locationListener);
            locationService.removeAlarmTriggerListener(alarmTriggerListener);
            locationService.stopLocation();
        }
        
        // 停止前台服务
        stopForeground(true);
        
        // 停止当前服务
        stopSelf();
        
        // 通过系统API终止应用进程
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}