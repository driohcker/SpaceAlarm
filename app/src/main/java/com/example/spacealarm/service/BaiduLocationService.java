package com.example.spacealarm.service;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.manager.BaiduMapManager;
import com.example.spacealarm.service.NotificationService;

public class BaiduLocationService {
    private static final String TAG = "BaiduLocationService";

    private static BaiduLocationService instance;

    private LocationClient locationClient;
    private AlarmService alarmService;
    private NotificationService notificationService; // 新增通知服务实例

    private OnLocationChangedListener listener;
    private boolean isStarted = false;

    private static Context appContext;

    public interface OnLocationChangedListener {
        void onLocationChanged(double latitude, double longitude, float accuracy, String address);
        void onAlarmTriggered(Alarm alarm, double latitude, double longitude);
        void onLocationError(int errorCode);
    }

    // 私有构造函数防止外部实例化
    private BaiduLocationService(Context context) throws Exception {
        this.alarmService = new AlarmService(context);
        this.notificationService = new NotificationService(context); // 初始化通知服务
        initLocationClient(context);
    }

    // 单例获取方法
    public static BaiduLocationService getInstance(Context context) {
        // 强制使用Application上下文
        appContext = context.getApplicationContext();

        if (instance == null) {
            synchronized (BaiduLocationService.class) {
                if (instance == null) {
                    // 确保SDK已初始化并同意隐私政策
                    if (!BaiduMapManager.isInitialized()) {
                        Log.d(TAG, "未初始化BaiduMapManager，现在开始初始化BaiduMapManager");
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

    private void initLocationClient(Context context) throws Exception {

        // 使用Application上下文创建LocationClient
        Log.d(TAG, "开始创建locationClient对象");
        locationClient = new LocationClient(appContext);
        Log.d(TAG, "创建locationClient对象完成");

        // 配置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
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

                    // 通知位置变化
                    if (listener != null) {
                        listener.onLocationChanged(latitude, longitude, accuracy, address);
                    }

                    // 检查是否触发闹钟
                    checkAlarmTrigger(latitude, longitude);

                } else {
                    Log.e(TAG, "Location error, type: " + bdLocation.getLocType());
                    if (listener != null) {
                        listener.onLocationError(bdLocation.getLocType());
                    }
                }
            }
        });
    }

    private void checkAlarmTrigger(double latitude, double longitude) {
        Alarm triggeredAlarm = alarmService.checkAllAlarms(latitude, longitude);
        if (triggeredAlarm != null) {
            Log.d(TAG, "Alarm triggered: " + triggeredAlarm.getTitle());
            // 显示通知
            notificationService.showAlarmNotification(triggeredAlarm);
            // 通知监听器
            if (listener != null) {
                listener.onAlarmTriggered(triggeredAlarm, latitude, longitude);
            }
        }
    }

    public void startLocation() {
        if (!isStarted && locationClient != null) {
            locationClient.start();
            isStarted = true;
            Log.d(TAG, "Baidu location service started");
        }
    }

    public void stopLocation() {
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
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

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        this.listener = listener;
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

    // 新增：设置AlarmService的方法，支持依赖注入
    public void setAlarmService(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    // 更新地图上的我的位置
    public static void updateMyLocation(BaiduMap baiduMap, double latitude, double longitude, float accuracy) {
        if (baiduMap != null) {
            MyLocationData locationData = new MyLocationData.Builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .accuracy(accuracy)
                    .direction(0)
                    .build();
            baiduMap.setMyLocationData(locationData);
        }
    }

    // 将地图中心移动到指定位置
    public static void centerMapToLocation(BaiduMap baiduMap, double latitude, double longitude, float zoom) {
        if (baiduMap != null) {
            LatLng location = new LatLng(latitude, longitude);
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(location, zoom);
            baiduMap.animateMapStatus(mapStatusUpdate);
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