package com.example.spacealarm.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
            // 提示用户缺少的权限，并再次请求
            Toast.makeText(this, "请授权所有必要权限以使用应用功能", Toast.LENGTH_LONG).show();

            //这一段是不必要的，只需要提醒用户就会好了，不然我会觉得你一直提醒的很烦
            //requestAllPermissions();
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
}