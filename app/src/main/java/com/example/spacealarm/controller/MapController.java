package com.example.spacealarm.controller;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.example.spacealarm.R;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.AlarmService;
import com.example.spacealarm.service.BaiduLocationService;
import com.example.spacealarm.service.manager.BaiduMapManager;

import java.util.ArrayList;
import java.util.List;

// 添加必要的导入
import com.baidu.mapapi.search.core.PoiInfo;

public class MapController {
    private final Context context;
    private final BaiduMap baiduMap;
    private final MapView mapView;
    private final AlarmService alarmService;
    private final BaiduLocationService locationService;

    private final List<Marker> alarmMarkers = new ArrayList<>();
    private final List<Marker> poiMarkers = new ArrayList<>();

    private OnMapInteractionListener listener;
    private GeoCoder geoCoder;

    public interface OnMapInteractionListener {
        void onMapClick(LatLng latLng, String address);
        void onMarkerClick(Alarm alarm);
        void onLocationUpdate(LatLng currentLocation);
        // 添加新方法用于处理POI标记点击
        void onPoiMarkerClick(LatLng latLng, String name, String address);
    }

    public MapController(Context context, MapView mapView) {
        this.context = context.getApplicationContext();
        this.mapView = mapView;
        this.baiduMap = mapView.getMap();

        this.alarmService = new AlarmService(context);
        this.locationService = BaiduLocationService.getInstance(context);

        initMap();
        initLocation();
        loadAlarms();
    }

    // 添加一个临时标记用于预览
    private Marker tempMarker;
    private LatLng lastClickedPosition;

    private void initMap() {
        // 设置地图类型为普通地图
        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);

        // 禁用缩放控件
        mapView.showZoomControls(false);

        // 设置地图点击事件
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // 记录最后点击的位置
                lastClickedPosition = latLng;
                
                // 移除之前的临时标记
                if (tempMarker != null) {
                    tempMarker.remove();
                }
                
                // 创建临时标记图标
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_temp_marker);
                
                // 创建标记选项
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .icon(bitmapDescriptor)
                        .title("点击添加闹钟")
                        .draggable(true);
                
                // 添加标记到地图
                tempMarker = (Marker) baiduMap.addOverlay(markerOptions);
                
                // 获取点击位置的地址
                getAddressFromLatLng(latLng);
            }

            @Override
            public void onMapPoiClick(MapPoi mapPoi) {
                if (listener != null) {
                    listener.onMapClick(mapPoi.getPosition(), mapPoi.getName());
                }
            }
        });

        // 初始化地理编码器
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                // 正向地理编码结果，这里不需要
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null) {
                    Log.e("MapController", "逆地理编码结果为空");
                    Toast.makeText(context, "获取地址失败: 服务无响应", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (reverseGeoCodeResult.error != null && reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.e("MapController", "逆地理编码错误: " + reverseGeoCodeResult.error);
                    Toast.makeText(context, "获取地址失败: " + reverseGeoCodeResult.error, Toast.LENGTH_SHORT).show();
                    return;
                }

                String address = reverseGeoCodeResult.getAddress();
                LatLng location = reverseGeoCodeResult.getLocation();

                if (address == null || address.trim().isEmpty()) {
                    Log.e("MapController", "获取到的地址为空");
                    Toast.makeText(context, "获取地址失败: 未找到地址信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (listener != null) {
                    listener.onMapClick(location, address);
                }
            }
        });

        // 设置标记点击事件
        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // 检查是否是闹钟标记
                Alarm alarm = (Alarm) marker.getExtraInfo().getSerializable("alarm");
                if (alarm != null && listener != null) {
                    listener.onMarkerClick(alarm);
                    return true;
                }
                
                // 检查是否是POI标记
                String poiName = marker.getExtraInfo().getString("poi_name");
                if (poiName != null && listener != null) {
                    double latitude = marker.getExtraInfo().getDouble("poi_latitude");
                    double longitude = marker.getExtraInfo().getDouble("poi_longitude");
                    String address = marker.getExtraInfo().getString("poi_address");
                    LatLng latLng = new LatLng(latitude, longitude);
                    listener.onPoiMarkerClick(latLng, poiName, address);
                    return true;
                }
                return false;
            }
        });
    }

    private void initLocation() {
        // 设置定位回调
        locationService.setOnLocationChangedListener(new BaiduLocationService.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude, float accuracy, String address) {
                LatLng currentLocation = new LatLng(latitude, longitude);

                // 使用BaiduMapManager更新地图位置
                BaiduMapManager.centerMapToLocation(baiduMap, latitude, longitude, 15.0f);
                BaiduMapManager.updateMyLocation(baiduMap, latitude, longitude, accuracy);

                if (listener != null) {
                    listener.onLocationUpdate(currentLocation);
                }
            }

            @Override
            public void onAlarmTriggered(Alarm alarm, double latitude, double longitude) {
                // 处理闹钟触发逻辑
            }

            @Override
            public void onLocationError(int errorCode) {
                // 处理定位错误
            }
        });
    }

    public void startLocation() {
        locationService.startLocation();
    }

    public void stopLocation() {
        locationService.stopLocation();
    }

    // 显示闹钟标记
    public void showAlarmMarker(Alarm alarm) {
        if (alarm == null) return;

        LatLng location = new LatLng(alarm.getLatitude(), alarm.getLongitude());

        // 创建标记图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_alarm_marker_32);

        // 创建标记选项
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .icon(bitmapDescriptor)
                .title(alarm.getTitle())
                .draggable(false);

        // 添加标记到地图
        Marker marker = (Marker) baiduMap.addOverlay(markerOptions);

        // 保存闹钟信息到标记
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putSerializable("alarm", alarm);
        marker.setExtraInfo(bundle);

        alarmMarkers.add(marker);

        // 添加圆形范围
        showCircleOverlay(location, alarm.getRadius());
        // 添加标题
        showTextOverlay(location, alarm.getTitle());
    }

    // 在闹钟标记周围添加范围显示
    private void showCircleOverlay(LatLng center, double radius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius((int) radius)
                .fillColor(Color.argb(30, 102, 102, 102))
                .stroke(new Stroke(2, Color.argb(100, 102, 102, 102)));

        baiduMap.addOverlay(circleOptions);
    }

    // 在闹钟标记下添加title字段
    private void showTextOverlay(LatLng center, String title){
        // 在标记下方添加文本覆盖物显示标题
        // 计算文本位置（在标记下方约30像素处）
        LatLng textLocation = new LatLng(center.latitude - 0.0025, center.longitude);

        // 创建文本选项
        TextOptions textOptions = new TextOptions()
                .position(textLocation)
                .text(title)
                .fontSize(20)
                .fontColor(0xFF333333)
                .bgColor(0x00000000)  // 透明背景
                .rotate(0);

        baiduMap.addOverlay(textOptions);
    }

    //移除闹钟标记
    public void removeAlarmMarker(Alarm alarm) {
        for (Marker marker : alarmMarkers) {
            Alarm markerAlarm = (Alarm) marker.getExtraInfo().getSerializable("alarm");
            if (markerAlarm != null && markerAlarm.getId() == alarm.getId()) {
                marker.remove();
                alarmMarkers.remove(marker);
                break;
            }
        }

        // 重新加载所有闹钟以刷新显示
        clearAllMarkers();
        loadAlarms();
    }

    // 更新所有闹钟标记
    public void updateAlarmMarker(Alarm alarm) {
        removeAlarmMarker(alarm);
        showAlarmMarker(alarm);
    }

    //显示所有闹钟标记
    public void loadAlarms() {
        clearAllMarkers();

        List<Alarm> alarms = alarmService.getAllAlarms();
        for (Alarm alarm : alarms) {
            if (alarm.getLatitude() != 0 && alarm.getLongitude() != 0) {
                showAlarmMarker(alarm);
            }
        }
    }

    // 显示POI标记
    public void showPoiMarker(PoiInfo poi) {
        LatLng location = poi.location;
        if (location == null) return;
    
        // 创建POI标记图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_poi_marker);
    
        // 创建标记选项
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .icon(bitmapDescriptor)
                .title(poi.name)
                .draggable(false);
    
        // 添加标记到地图
        Marker marker = (Marker) baiduMap.addOverlay(markerOptions);
    
        // 保存POI信息到标记 - 改为存储单独的属性而非整个对象
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putString("poi_name", poi.name);
        bundle.putDouble("poi_latitude", location.latitude);
        bundle.putDouble("poi_longitude", location.longitude);
        bundle.putString("poi_address", poi.address);
        bundle.putString("poi_uid", poi.uid);
        marker.setExtraInfo(bundle);
    
        poiMarkers.add(marker);
    
        // 添加标题
        showTextOverlay(location, poi.name);
    }

    // 缩放地图以显示所有POI
    public void zoomToFitAllPois(List<PoiInfo> poiList) {
        if (poiList.isEmpty()) return;

        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;

        for (PoiInfo poi : poiList) {
            if (poi.location != null) {
                minLat = Math.min(minLat, poi.location.latitude);
                maxLat = Math.max(maxLat, poi.location.latitude);
                minLng = Math.min(minLng, poi.location.longitude);
                maxLng = Math.max(maxLng, poi.location.longitude);
            }
        }

        LatLng northeast = new LatLng(maxLat, maxLng);
        LatLng southwest = new LatLng(minLat, minLng);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(northeast)
                .include(southwest)
                .build();

        baiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngBounds(bounds));
    }

    // 清除所有POI标记
    public void clearAllPoiMarkers() {
        for (Marker marker : poiMarkers) {
            marker.remove();
        }
        poiMarkers.clear();
    }

    // 更新clearAllMarkers方法以同时清除POI标记
    public void clearAllMarkers() {
        baiduMap.clear();
        alarmMarkers.clear();
        clearAllPoiMarkers();
    }

    private void getAddressFromLatLng(LatLng latLng) {
        if (geoCoder != null && latLng != null) {
            ReverseGeoCodeOption option = new ReverseGeoCodeOption().location(latLng);
            geoCoder.reverseGeoCode(option);
        }
    }

    public void setOnMapInteractionListener(OnMapInteractionListener listener) {
        this.listener = listener;
    }

    public void onResume() {
        mapView.onResume();
        startLocation();
    }

    public void onPause() {
        mapView.onPause();
        stopLocation();
    }

    public void onDestroy() {
        if (geoCoder != null) {
            geoCoder.destroy();
        }
        locationService.stopLocation();
        mapView.onDestroy();
    }

    public BaiduMap getBaiduMap() {
        return baiduMap;
    }

    public void setMapType(int mapType) {
        baiduMap.setMapType(mapType);
    }

    public void zoomToFitAllAlarms() {
        List<Alarm> alarms = alarmService.getAllAlarms();
        if (alarms.isEmpty()) return;

        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLng = Double.MAX_VALUE;
        double maxLng = Double.MIN_VALUE;

        for (Alarm alarm : alarms) {
            minLat = Math.min(minLat, alarm.getLatitude());
            maxLat = Math.max(maxLat, alarm.getLatitude());
            minLng = Math.min(minLng, alarm.getLongitude());
            maxLng = Math.max(maxLng, alarm.getLongitude());
        }

        LatLng northeast = new LatLng(maxLat, maxLng);
        LatLng southwest = new LatLng(minLat, minLng);

        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(
                new com.baidu.mapapi.model.LatLngBounds.Builder().include(northeast).include(southwest).build()
        );
        baiduMap.animateMapStatus(mapStatusUpdate);
    }

    public void centerToMyLocation() {
        // 确保定位服务已启动
        if (!locationService.isStarted()) {
            locationService.startLocation();
        }
        
        // 尝试获取最后已知的位置
        BDLocation lastLocation = locationService.getLastKnownLocation();
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            float accuracy = lastLocation.getRadius();
            
            // 将地图中心移动到最后已知位置
            LatLng currentLocation = new LatLng(latitude, longitude);
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(currentLocation, 15.0f);
            baiduMap.animateMapStatus(mapStatusUpdate);
            
            // 更新我的位置标记
            MyLocationData locationData = new MyLocationData.Builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .accuracy(accuracy)
                    .direction(0)
                    .build();
            baiduMap.setMyLocationData(locationData);
        } else {
            // 如果没有最后已知位置，则请求一次定位更新
            locationService.requestLocation();
            
            // 显示提示信息
            Toast.makeText(context, "正在获取您的位置...", Toast.LENGTH_SHORT).show();
        }
    }
    
    // 获取最后点击的位置
    public LatLng getLastClickedPosition() {
        return lastClickedPosition;
    }

    // 在MapController类中添加showAlarmLocation方法
    public void showAlarmLocation(long alarmId) {
        // 根据alarmId获取闹钟对象
        Alarm alarm = alarmService.getAlarmById(alarmId);
        if (alarm != null) {
            LatLng alarmLocation = new LatLng(alarm.getLatitude(), alarm.getLongitude());
            // 定位到闹钟位置
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(alarmLocation, 15));
            // 在地图上标记闹钟位置
            updateAlarmMarker(alarm);
        }
    }
}

