package com.example.spacealarm.service;

import android.content.Context;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.listener.AlarmTriggerListener;
import com.example.spacealarm.service.listener.LocationListener;
// 修改导入语句
import com.example.spacealarm.service.manager.BaiduMapManager;
// 删除错误的导入
// import com.example.spacealarm.service.manager.NotificationManager;
// 添加正确的导入
import com.example.spacealarm.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BaiduLocationService {
    private static final String TAG = "BaiduLocationService";
    private static BaiduLocationService instance;
    private final Context appContext;
    private LocationClient locationClient;
    private final AlarmService alarmService;
    // 修改成员变量声明
    private final NotificationService notificationService;
    
    // 使用CopyOnWriteArrayList确保线程安全
    private final List<LocationListener> locationListeners = new CopyOnWriteArrayList<>();
    private final List<AlarmTriggerListener> alarmTriggerListeners = new CopyOnWriteArrayList<>();
    
    private boolean isStarted = false;

    // 私有构造函数防止外部实例化
    private BaiduLocationService(Context context) throws Exception {
        this.appContext = context.getApplicationContext();
        this.alarmService = new AlarmService(appContext);
        // 修改初始化
        this.notificationService = NotificationService.getInstance(appContext);
        initLocationClient();
    }

    // 单例获取方法
    public static BaiduLocationService getInstance(Context context) {
        // 强制使用Application上下文
        Context appContext = context.getApplicationContext();

        if (instance == null) {
            synchronized (BaiduLocationService.class) {
                if (instance == null) {
                    // 确保SDK已初始化并同意隐私政策
                    if (!BaiduMapManager.isInitialized()) {
                        Log.d(TAG, "未初始化BaiduMapManager，现在开始初始化");
                        BaiduMapManager.initialize(appContext);
                    }

                    try {
                        Log.d(TAG, "开始创建BaiduLocationService服务");
                        instance = new BaiduLocationService(appContext);
                        Log.d(TAG, "BaiduLocationService服务创建成功");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }

    private void initLocationClient() throws Exception {
        // 使用Application上下文创建LocationClient
        Log.d(TAG, "开始创建locationClient对象");
        locationClient = new LocationClient(appContext);
        Log.d(TAG, "创建locationClient对象完成");

        // 配置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll"); // 百度坐标系
        option.setScanSpan(5000); // 5秒扫描一次
        option.setIsNeedAddress(true); // 需要地址信息
        option.setIsNeedLocationDescribe(true); // 需要位置描述
        option.setOpenGps(true); // 打开GPS
        option.setLocationNotify(true); // 当GPS有效时按照1次/秒的频率输出GPS结果
        option.setIsNeedLocationPoiList(true); // 需要POI信息
        option.setIgnoreKillProcess(false); // 定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false); // 可选，设置是否收集Crash信息，默认收集
        option.setEnableSimulateGps(false); // 可选，设置是否需要过滤GPS仿真结果，默认需要

        locationClient.setLocOption(option);

        // 设置定位监听器
        locationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null) {
                    Log.e(TAG, "BDLocation is null");
                    return;
                }

                if (bdLocation.getLocType() == BDLocation.TypeGpsLocation ||
                        bdLocation.getLocType() == BDLocation.TypeNetWorkLocation ||
                        bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {

                    double latitude = bdLocation.getLatitude();
                    double longitude = bdLocation.getLongitude();
                    float accuracy = bdLocation.getRadius();
                    String address = bdLocation.getAddrStr(); // 获取地址信息
                    if (address == null || address.isEmpty()) {
                        address = "未知位置";
                    }

                    Log.d(TAG, "Location received: " + latitude + ", " + longitude + ", accuracy: " + accuracy + ", address: " + address);

                    // 通知所有位置监听器
                    notifyLocationChanged(latitude, longitude, accuracy, address);

                    // 检查是否触发闹钟
                    checkAlarmTrigger(latitude, longitude);

                } else {
                    Log.e(TAG, "Location error, type: " + bdLocation.getLocType());
                    // 通知所有位置监听器
                    notifyLocationError(bdLocation.getLocType());
                }
            }
        });
    }

    private void checkAlarmTrigger(double latitude, double longitude) {
        Alarm triggeredAlarm = alarmService.checkAllAlarms(latitude, longitude);
        if (triggeredAlarm != null) {
            Log.d(TAG, "检测到闹钟触发：" + triggeredAlarm.getTitle());
            // 修改通知调用
            notificationService.showAlarmNotification(triggeredAlarm);
            // 通知所有闹钟触发监听器
            notifyAlarmTriggered(triggeredAlarm, latitude, longitude);
        }
    }

    // 通知所有位置监听器
    private void notifyLocationChanged(double latitude, double longitude, float accuracy, String address) {
        for (LocationListener listener : locationListeners) {
            try {
                listener.onLocationChanged(latitude, longitude, accuracy, address);
            } catch (Exception e) {
                Log.e(TAG, "Location listener error: " + e.getMessage());
            }
        }
    }

    // 通知所有位置错误监听器
    private void notifyLocationError(int errorCode) {
        for (LocationListener listener : locationListeners) {
            try {
                listener.onLocationError(errorCode);
            } catch (Exception e) {
                Log.e(TAG, "Location error listener error: " + e.getMessage());
            }
        }
    }

    // 通知所有闹钟触发监听器
    private void notifyAlarmTriggered(Alarm alarm, double latitude, double longitude) {
        for (AlarmTriggerListener listener : alarmTriggerListeners) {
            try {
                listener.onAlarmTriggered(alarm, latitude, longitude);
            } catch (Exception e) {
                Log.e(TAG, "Alarm trigger listener error: " + e.getMessage());
            }
        }
    }

    // 监听器管理方法
    public void addLocationListener(LocationListener listener) {
        if (listener != null && !locationListeners.contains(listener)) {
            locationListeners.add(listener);
        }
    }

    public void removeLocationListener(LocationListener listener) {
        if (listener != null) {
            locationListeners.remove(listener);
        }
    }

    public void addAlarmTriggerListener(AlarmTriggerListener listener) {
        if (listener != null && !alarmTriggerListeners.contains(listener)) {
            alarmTriggerListeners.add(listener);
        }
    }

    public void removeAlarmTriggerListener(AlarmTriggerListener listener) {
        if (listener != null) {
            alarmTriggerListeners.remove(listener);
        }
    }

    // 定位控制方法
    public void startLocation() {
        if (!isStarted && locationClient != null) {
            try {
                locationClient.start();
                isStarted = true;
                Log.d(TAG, "Baidu location service started");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start location service: " + e.getMessage(), e);
            }
        }
    }

    public void stopLocation() {
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            isStarted = false;
            Log.d(TAG, "定位已停止");
        }
    }

    public void restartLocation() {
        stopLocation();
        startLocation();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public BDLocation getLastKnownLocation() {
        if (locationClient != null) {
            return locationClient.getLastKnownLocation();
        }
        return null;
    }

    public void requestLocation() {
        if (locationClient != null && isStarted) {
            locationClient.requestLocation();
        }
    }

    public String getCurrentCity() {
        BDLocation location = getLastKnownLocation();
        if (location != null) {
            return location.getCity();
        }
        return null;
    }
}