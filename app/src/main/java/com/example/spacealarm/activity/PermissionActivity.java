package com.example.spacealarm.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
// import android.widget.ImageButton;  // 移除这行
import androidx.appcompat.widget.AppCompatButton;  // 添加这行
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spacealarm.AppApplication;
import com.example.spacealarm.R;
import com.example.spacealarm.service.TextToSpeechManager;
import com.example.spacealarm.service.manager.BaiduMapManager;
import com.example.spacealarm.service.manager.PermissionManager;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";
    private AppCompatButton authorizeButton;  // 改为AppCompatButton
    private AppCompatButton cancelButton;  // 改为AppCompatButton
    private CheckBox baiduAgreementCheckbox;
    private TextView baiduAgreementText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // 初始化按钮和复选框
        authorizeButton = findViewById(R.id.authorize_button);
        cancelButton = findViewById(R.id.cancel_button);
        baiduAgreementCheckbox = findViewById(R.id.baidu_agreement_checkbox);
        baiduAgreementText = findViewById(R.id.baidu_agreement_text);

        // 设置百度协议点击事件
        baiduAgreementText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到百度地图隐私政策页面
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://lbsyun.baidu.com/index.php?title=openprivacy"));
                startActivity(intent);
            }
        });
        /**
         * 设置勾选框点击事件
         * @author blgnni
         */
        baiduAgreementCheckbox.setOnClickListener(v -> {
            if (baiduAgreementCheckbox.isChecked()) {
                // 设置按钮为激活状态（颜色变深）
                authorizeButton.setBackgroundResource(R.drawable.rounded_background_blue_active);
            } else {
                // 恢复未激活状态
                authorizeButton.setBackgroundResource(R.drawable.rounded_background_blue);
            }
        });

        // 设置按钮点击事件
        authorizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (baiduAgreementCheckbox.isChecked()) {
                    requestAllPermissions();
                } else {
                    Toast.makeText(PermissionActivity.this, "请阅读并同意百度地图政策协议", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果用户取消授权，直接退出应用
                finishAffinity();
                System.exit(0);
            }
        });

        // 检查是否已经拥有所有权限
        if (PermissionManager.checkLocationPermissions(this)) {
            Log.d(TAG, "所有权限已授权，直接进入主界面");
            // 这里假设用户已经同意过协议，直接初始化SDK并进入主界面
            initializeBaiduMapSDK();
            initTTS();
            startMainActivity();
        }
    }

    private void requestAllPermissions() {
        PermissionManager.requestLocationPermissions(this);
    }

    private void initTTS(){
        try {
            TextToSpeechManager textToSpeechManager = TextToSpeechManager.getInstance(getApplicationContext());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeBaiduMapSDK() {
        try {
            // 初始化百度地图SDK - 使用ApplicationContext替代Activity上下文
            BaiduMapManager.initialize(getApplicationContext());
            AppApplication.isPermissionsGranted = true;
        } catch (Exception e) {
            Log.e(TAG, "百度地图SDK初始化失败: " + e.getMessage());
            Toast.makeText(this, "百度地图初始化失败，请重启应用", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allGranted = PermissionManager.onRequestPermissionsResult(requestCode, grantResults);

        if (allGranted && baiduAgreementCheckbox.isChecked()) {
            Log.d(TAG, "所有权限授权成功且已同意百度协议");
            // 所有权限都授权了，初始化百度地图SDK
            initializeBaiduMapSDK();
            startMainActivity();
        } else if (!baiduAgreementCheckbox.isChecked()) {
            Toast.makeText(this, "请阅读并同意百度地图政策协议", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "部分或全部权限未授权");
            // 获取未授权的权限列表
            StringBuilder missingPermissions = new StringBuilder();
            for (String permission : PermissionManager.LOCATION_PERMISSIONS) {
                // 跳过空字符串权限（Android 13以下设备上的细粒度权限）
                if (permission.isEmpty()) continue;
                
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    // 获取权限的显示名称
                    String permissionName = getPermissionDisplayName(permission);
                    missingPermissions.append("• ").append(permissionName).append("\n");
                }
            }
            // 提示用户缺少的具体权限，并再次请求
            String message = "请授权权限以使用应用功能：\n" + missingPermissions.toString();
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    // 修改startMainActivity方法
    private void startMainActivity() {
        // 检查优化设置是否已完成
        android.content.SharedPreferences prefs = getSharedPreferences(AppApplication.PREFS_NAME, MODE_PRIVATE);
        boolean isOptimizationCompleted = prefs.getBoolean(AppApplication.KEY_OPTIMIZATION_COMPLETED, false);
        
        if (isOptimizationCompleted) {
            // 如果优化设置已完成，直接跳转到主界面
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            // 如果优化设置未完成，跳转到优化设置页面
            Intent intent = new Intent(this, OptimizeSettingsActivity.class);
            startActivity(intent);
        }
        finish(); // 结束当前Activity，避免用户返回到这个权限请求界面
    }

    // 防止用户通过返回按钮跳过权限请求
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // 不执行任何操作，或直接退出应用
        finishAffinity();
        System.exit(0);
        super.onBackPressed();
    }
    
    /**
     * 获取权限的友好显示名称
     * @param permission 权限名称
     * @return 友好的显示名称
     */
    private String getPermissionDisplayName(String permission) {
        if (permission.equals(Manifest.permission.POST_NOTIFICATIONS)) {
            return "通知权限";
        } else if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
            return "精确位置权限";
        } else if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            return "粗略位置权限";
        } else if (permission.equals(Manifest.permission.ACCESS_WIFI_STATE)) {
            return "Wi-Fi状态权限";
        } else if (permission.equals(Manifest.permission.ACCESS_NETWORK_STATE)) {
            return "网络状态权限";
        } else if (permission.equals(Manifest.permission.CHANGE_WIFI_STATE)) {
            return "修改Wi-Fi状态权限";
        } else if (permission.equals(Manifest.permission.READ_PHONE_STATE)) {
            return "读取手机状态权限";
        } else if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return "存储权限";
        } else if (permission.equals(Manifest.permission.READ_MEDIA_IMAGES)) {
            return "照片权限";
        } else if (permission.equals(Manifest.permission.READ_MEDIA_AUDIO)) {
            return "音乐与音频权限";
        } else if (permission.equals(Manifest.permission.READ_MEDIA_VIDEO)) {
            return "视频权限";
        } else if (permission.equals(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
            return "文件与文档权限";
        } else {
            // 默认返回权限的简单名称
            int lastDotIndex = permission.lastIndexOf('.');
            if (lastDotIndex >= 0 && lastDotIndex < permission.length() - 1) {
                return permission.substring(lastDotIndex + 1).replace('_', ' ');
            }
            return permission;
        }
    }
}