package com.example.spacealarm.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.spacealarm.AppApplication;
import com.example.spacealarm.R;
import com.example.spacealarm.activity.widget.CustomBottomNavigation;
import com.example.spacealarm.service.manager.CustomToolbarManager;
import com.example.spacealarm.fragment.AlarmFragment;
import com.example.spacealarm.fragment.MapFragment;
import com.example.spacealarm.fragment.SettingsFragment;
import com.example.spacealarm.service.BaiduLocationService;
import com.example.spacealarm.service.ForegroundLocationService;
import com.example.spacealarm.service.manager.PermissionManager;
import com.example.spacealarm.controller.SettingsController;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private FragmentManager fragmentManager;
    private AlarmFragment alarmFragment;
    private MapFragment mapFragment;
    private SettingsFragment settingsFragment;
    private Fragment currentFragment;

    private LinearLayout navLayout;

    private BaiduLocationService baiduLocationService;
    private SettingsController settingsController;

    // 在onCreate方法中，修改启动前台服务的逻辑
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        // 标记权限已授权
        AppApplication.isPermissionsGranted = true;
        Log.d(TAG, "权限已授权，开始初始化应用业务");
    
        // 初始化底部导航
        navLayout = findViewById(R.id.custom_bottom_nav);
        CustomBottomNavigation.setup(this, navLayout);
    
        // 初始化Toolbar管理器
        CustomToolbarManager.setup(this);
        
        // 初始化设置控制器
        settingsController = SettingsController.getInstance(this);
        
        // 初始化定位服务
        initializeLocationService();
        
        // 根据设置决定是否启动前台服务 - 保持原逻辑不变
        if (settingsController.isBackgroundServiceEnabled()) {
            startForegroundService();
        }
    }

    // 初始化定位服务
    private void initializeLocationService() {
        try {
            // 延迟初始化定位服务单例，确保SDK完全就绪
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try {
                    Log.d(TAG, "开始初始化定位服务");
                    baiduLocationService = BaiduLocationService.getInstance(this);
                    Log.d(TAG, "定位服务初始化成功");
                } catch (Exception e) {
                    Log.e(TAG, "定位服务初始化失败: " + e.getMessage());
                    showToast("定位服务初始化失败: " + e.getMessage());
                }
            }, 500); // 延迟500ms确保SDK初始化完成
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "定位服务初始化异常: " + e.getMessage());
        }
    }

    // 启动前台服务
    private void startForegroundService() {
        try {
            Intent serviceIntent = new Intent(this, ForegroundLocationService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Log.d(TAG, "前台服务启动成功");
        } catch (Exception e) {
            Log.e(TAG, "启动前台服务失败: " + e.getMessage());
            showToast("启动定位服务失败: " + e.getMessage());
        }
    }

    // 权限处理方法
    @Override
    protected void onPermissionsResult(boolean granted) {
        if (granted && mapFragment != null && mapFragment.isVisible()) {
            mapFragment.onResume(); // 权限获取后刷新地图
        }
    }
}