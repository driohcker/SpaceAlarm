package com.example.spacealarm.service.manager;

import android.content.Context;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class BaiduMapManager {
    private static final String TAG = "BaiduMapManager";
    private static boolean isInitialized = false;
    private static final Object lock = new Object();

    public static void initialize(Context context) {
        synchronized (lock) {
            if (!isInitialized) {
                try {
                    SDKInitializer.setAgreePrivacy(context, true);
                    LocationClient.setAgreePrivacy(true);
                    SDKInitializer.initialize(context);
                    isInitialized = true;
                    Log.d(TAG, "百度地图SDK初始化成功");
                } catch (Exception e) {
                    Log.e(TAG, "百度地图SDK初始化失败: " + e.getMessage());
                    throw new RuntimeException("百度地图SDK初始化失败", e);
                }
            }
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void centerMapToLocation(BaiduMap baiduMap, double latitude, double longitude, float zoomLevel) {
        LatLng latLng = new LatLng(latitude, longitude);
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        baiduMap.animateMapStatus(mapStatusUpdate);
    }

    public static void updateMyLocation(BaiduMap baiduMap, double latitude, double longitude, float accuracy) {
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(accuracy)
                .direction(0)
                .latitude(latitude)
                .longitude(longitude)
                .build();
        baiduMap.setMyLocationData(locData);
    }
}