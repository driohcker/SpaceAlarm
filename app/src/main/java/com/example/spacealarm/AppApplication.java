package com.example.spacealarm;

import android.app.Application;
import android.util.Log;

public class AppApplication extends Application {
    private static String TAG = "AppApplication";
    
    // 标记应用是否已完成所有必要权限授权
    public static boolean isPermissionsGranted = false;
    
    // 用于SharedPreferences的常量
    public static final String PREFS_NAME = "app_settings";
    public static final String KEY_OPTIMIZATION_COMPLETED = "optimization_completed";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "应用初始化完成");
        // 注意：百度地图SDK初始化已移至PermissionActivity，需要用户同意协议后再初始化
    }
}