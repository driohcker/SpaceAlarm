package com.example.spacealarm;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.spacealarm.service.BaiduLocationService;
import com.example.spacealarm.service.ForegroundLocationService;
import com.example.spacealarm.service.manager.BaiduMapManager;

public class AppApplication extends Application {
    private static String TAG = "AppApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // 全局初始化百度地图SDK
        //BaiduMapManager.initialize(this);
        
        try {
            // 延迟初始化定位服务单例，确保SDK完全就绪
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Log.d(TAG, "开始初始化定位服务");
                    BaiduLocationService.getInstance(this);
                    Log.d(TAG, "定位服务预初始化成功");
                } catch (Exception e) {
                    Log.e(TAG, "定位服务预初始化失败: " + e.getMessage());
                }
            }, 500); // 延迟500ms确保SDK初始化完成
        } catch (Exception e) {
            e.printStackTrace();
        }

        startForegroundService();
    }

    private void startForegroundService() {
    Intent serviceIntent = new Intent(this, ForegroundLocationService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
    } else {
        startService(serviceIntent);
    }
}
}