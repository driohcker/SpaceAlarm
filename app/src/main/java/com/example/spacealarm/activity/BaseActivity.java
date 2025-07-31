package com.example.spacealarm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spacealarm.R;
import com.example.spacealarm.service.BaiduLocationService;
import com.example.spacealarm.service.manager.BaiduMapManager;
import com.example.spacealarm.service.manager.PermissionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {
    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();


    }


    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showToastLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void showLoading() {
        // 可以添加统一的加载动画
    }

    protected void hideLoading() {
        // 隐藏加载动画
    }

    public void checkPermissions() {
        if (!PermissionManager.checkLocationPermissions(this)) {
            PermissionManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = PermissionManager.onRequestPermissionsResult(requestCode, grantResults);
        onPermissionsResult(granted);
    }

    protected abstract void onPermissionsResult(boolean granted);
}