package com.example.spacealarm.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.spacealarm.AppApplication;
import com.example.spacealarm.R;

public class OptimizeSettingsActivity extends AppCompatActivity {
    private static final String TAG = "OptimizeSettingsActivity";
    private AppCompatButton autostartButton;
    private AppCompatButton batteryOptButton;
    private AppCompatButton continueButton;
    private boolean isAutostartEnabled = false;
    private boolean isBatteryOptDisabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optimize_settings);

        // 初始化按钮
        autostartButton = findViewById(R.id.autostart_button);
        batteryOptButton = findViewById(R.id.battery_opt_button);
        continueButton = findViewById(R.id.continue_button);

        // 设置按钮点击事件
        autostartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutostartSettings();
                // 点击后假设用户已设置（实际应用中可以添加检测逻辑）
                isAutostartEnabled = true;
                updateButtonStates();
            }
        });

        batteryOptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBatteryOptimizationSettings();
                // 点击后假设用户已设置（实际应用中可以添加检测逻辑）
                isBatteryOptDisabled = true;
                updateButtonStates();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        // 初始状态下继续按钮不可用
        continueButton.setEnabled(false);
    }

    private void openAutostartSettings() {
        try {
            // 不同厂商的自启动设置路径可能不同，这里提供常见的几种
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("packageName", getPackageName());
            
            // 小米
            ComponentName componentName = new ComponentName("com.miui.securitycenter", 
                    "com.miui.permcenter.autostart.AutoStartManagementActivity");
            intent.setComponent(componentName);
            
            if (null != intent.resolveActivity(getPackageManager())) {
                startActivity(intent);
                return;
            }
            
            // 华为
            componentName = new ComponentName("com.huawei.systemmanager", 
                    "com.huawei.systemmanager.optimize.process.ProtectActivity");
            intent.setComponent(componentName);
            
            if (null != intent.resolveActivity(getPackageManager())) {
                startActivity(intent);
                return;
            }
            
            // OPPO
            componentName = new ComponentName("com.oppo.safe", 
                    "com.oppo.safe.permission.startup.StartupAppListActivity");
            intent.setComponent(componentName);
            
            if (null != intent.resolveActivity(getPackageManager())) {
                startActivity(intent);
                return;
            }
            
            // VIVO
            componentName = new ComponentName("com.vivo.permissionmanager", 
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity");
            intent.setComponent(componentName);
            
            if (null != intent.resolveActivity(getPackageManager())) {
                startActivity(intent);
                return;
            }
            
            // 通用方法，跳转到应用信息页面
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "打开自启动设置失败: " + e.getMessage());
            Toast.makeText(this, "无法打开自启动设置，请手动设置", Toast.LENGTH_LONG).show();
        }
    }

    private void openBatteryOptimizationSettings() {
        try {
            // 跳转到电池优化设置页面
            Intent intent = new Intent();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
            } else {
                // Android 5.0及以下版本，跳转到应用信息页面
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
            }
            
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开电池优化设置失败: " + e.getMessage());
            Toast.makeText(this, "无法打开电池优化设置，请手动设置", Toast.LENGTH_LONG).show();
        }
    }

    private void updateButtonStates() {
        // 当两个设置都完成后，启用继续按钮
        continueButton.setEnabled(isAutostartEnabled && isBatteryOptDisabled);
    }

    // 修改startMainActivity方法
    private void startMainActivity() {
        // 记录优化设置已完成
        android.content.SharedPreferences prefs = getSharedPreferences(AppApplication.PREFS_NAME, MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(AppApplication.KEY_OPTIMIZATION_COMPLETED, true);
        editor.apply();
        
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // 修改onBackPressed方法，允许用户跳过但记录跳过状态
    @Override
    public void onBackPressed() {
        // 显示跳过确认对话框
        new android.app.AlertDialog.Builder(this)
                .setTitle("确认跳过")
                .setMessage("跳过这些优化设置可能会导致应用无法正常工作或提醒不及时，是否确定要跳过？")
                .setPositiveButton("确定跳过", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        // 记录优化设置已跳过
                        android.content.SharedPreferences prefs = getSharedPreferences(AppApplication.PREFS_NAME, MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(AppApplication.KEY_OPTIMIZATION_COMPLETED, true);
                        editor.apply();
                        startMainActivity();
                    }
                })
                .setNegativeButton("继续设置", null)
                .show();
    }
}