package com.example.spacealarm.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.spacealarm.R;
import com.example.spacealarm.activity.widget.CustomBottomNavigation;
import com.example.spacealarm.activity.widget.CustomToolbarManager;
import com.example.spacealarm.fragment.AlarmFragment;
import com.example.spacealarm.fragment.MapFragment;
import com.example.spacealarm.fragment.SettingsFragment;
import com.example.spacealarm.service.BaiduLocationService;
import com.example.spacealarm.service.manager.BaiduMapManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity {
    private FragmentManager fragmentManager;
    private AlarmFragment alarmFragment;
    private MapFragment mapFragment;
    private SettingsFragment settingsFragment;
    private Fragment currentFragment;

    private LinearLayout navLayout;

    private BaiduLocationService baiduLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化底部导航
        navLayout = findViewById(R.id.custom_bottom_nav);
        CustomBottomNavigation.setup(this, navLayout);

        // 初始化Toolbar管理器
        CustomToolbarManager.setup(this);
    }

    // 添加权限处理方法
    @Override
    protected void onPermissionsResult(boolean granted) {
        if (granted && mapFragment != null && mapFragment.isVisible()) {
            mapFragment.onResume(); // 权限获取后刷新地图
        }
    }
}