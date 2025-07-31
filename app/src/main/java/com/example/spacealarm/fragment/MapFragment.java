package com.example.spacealarm.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.example.spacealarm.R;
import com.example.spacealarm.activity.widget.CustomToolbarManager;
import com.example.spacealarm.controller.MapController;
import com.example.spacealarm.entity.Alarm;
import com.example.spacealarm.service.AlarmService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

// 添加必要的导入
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import java.util.List;

public class MapFragment extends Fragment implements MapController.OnMapInteractionListener {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;

    private FloatingActionButton fab_locate, fab_add_alarm;

    private MapController mapController;
    // 添加一个变量来存储最近点击位置的地址
    private String lastClickedAddress = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = view.findViewById(R.id.bmapView);
        fab_locate = view.findViewById(R.id.fab_locate);
        fab_add_alarm = view.findViewById(R.id.fab_add_alarm);


        // 使用Activity上下文而非应用上下文
        try {
            mapController = new MapController(getActivity(), mMapView);
            mapController.centerToMyLocation();
            mapController.startLocation();
            mapController.setOnMapInteractionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "地图初始化失败", Toast.LENGTH_SHORT).show();
        }

        fab_locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapController.centerToMyLocation();
            }
        });

        fab_add_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng clickedPosition = mapController.getLastClickedPosition();
                if (clickedPosition == null) {
                    Toast.makeText(getContext(), "请先在地图上点击选择位置", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // 显示添加闹钟对话框
                showAddAlarmDialog(clickedPosition);
            }
        });

        CustomToolbarManager.setMapFragment(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null  && mapController !=null) {
            mapController.onResume();
            mapController.centerToMyLocation();
            // 确保位置服务已启动
            mapController.startLocation();
        }
        // 检查定位权限
        checkLocationPermission();
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mapController.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            mapController.onDestroy();
        }
        super.onDestroy();
        // 清除MapFragment引用
        CustomToolbarManager.setMapFragment(null);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    // 显示添加闹钟对话框
    private void showAddAlarmDialog(final LatLng position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_alarm, null);
        builder.setView(dialogView);
        
        // 初始化对话框控件
        EditText editAlarmName = dialogView.findViewById(R.id.editAlarmName);
        Slider editAlarmRadius = dialogView.findViewById(R.id.editAlarmRadius);
        TextView radiusValue = dialogView.findViewById(R.id.radiusValue);
        SwitchMaterial switchVibration = dialogView.findViewById(R.id.switchVibration);
        SwitchMaterial switchSound = dialogView.findViewById(R.id.switchSound);
        
        // 更新半径显示
        radiusValue.setText((int)editAlarmRadius.getValue() + "米");
        editAlarmRadius.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                radiusValue.setText((int)value + "米");
            }
        });
        
        builder.setTitle("添加空间闹钟")
            .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // 获取输入值
                    String title = editAlarmName.getText().toString().trim();
                    float radius = editAlarmRadius.getValue();
                    boolean isVibrate = switchVibration.isChecked();
                    boolean isRing = switchSound.isChecked();
                    
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "请输入闹钟名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // 创建闹钟，使用获取到的地址
                    AlarmService alarmService = new AlarmService(getContext());
                    long alarmId = alarmService.createAlarmWithDetails(
                        title,
                        position.latitude,
                        position.longitude,
                        radius,
                        lastClickedAddress, // 使用存储的地址
                        "", // 可以添加默认消息
                        isVibrate,
                        isRing
                    );
                    
                    if (alarmId > 0) {
                        Toast.makeText(getContext(), "闹钟添加成功", Toast.LENGTH_SHORT).show();
                        // 刷新地图标记
                        mapController.loadAlarms();
                    } else {
                        Toast.makeText(getContext(), "闹钟添加失败", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 显示编辑闹钟对话框
    private void showEditAlarmDialog(final Alarm alarm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_alarm, null);
        builder.setView(dialogView);

        // 初始化对话框控件
        EditText editAlarmName = dialogView.findViewById(R.id.editAlarmName);
        Slider editAlarmRadius = dialogView.findViewById(R.id.editAlarmRadius);
        TextView radiusValue = dialogView.findViewById(R.id.radiusValue);
        SwitchMaterial switchVibration = dialogView.findViewById(R.id.switchVibration);
        SwitchMaterial switchSound = dialogView.findViewById(R.id.switchSound);

        // 填充现有闹钟数据
        editAlarmName.setText(alarm.getTitle());
        editAlarmRadius.setValue((float) alarm.getRadius());
        radiusValue.setText((int) alarm.getRadius() + "米");
        switchVibration.setChecked(alarm.isVibrate());
        switchSound.setChecked(alarm.isRing());

        // 更新半径显示
        editAlarmRadius.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                radiusValue.setText((int) value + "米");
            }
        });

        builder.setTitle("编辑空间闹钟")
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // 获取输入值
                        String title = editAlarmName.getText().toString().trim();
                        float radius = editAlarmRadius.getValue();
                        boolean isVibrate = switchVibration.isChecked();
                        boolean isRing = switchSound.isChecked();

                        if (title.isEmpty()) {
                            Toast.makeText(getContext(), "请输入闹钟名称", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 更新闹钟
                        alarm.setTitle(title);
                        alarm.setRadius(radius);
                        alarm.setVibrate(isVibrate);
                        alarm.setRing(isRing);

                        AlarmService alarmService = new AlarmService(getContext());
                        boolean success = alarmService.updateAlarm(alarm);

                        if (success) {
                            Toast.makeText(getContext(), "闹钟更新成功", Toast.LENGTH_SHORT).show();
                            // 刷新地图标记
                            mapController.updateAlarmMarker(alarm);
                        } else {
                            Toast.makeText(getContext(), "闹钟更新失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除闹钟确认
                        new AlertDialog.Builder(getActivity())
                                .setTitle("确认删除")
                                .setMessage("确定要删除这个闹钟吗？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AlarmService alarmService = new AlarmService(getContext());
                                        boolean success = alarmService.deleteAlarm(alarm.getId());
                                        if (success) {
                                            Toast.makeText(getContext(), "闹钟删除成功", Toast.LENGTH_SHORT).show();
                                            // 刷新地图标记
                                            mapController.removeAlarmMarker(alarm);
                                        } else {
                                            Toast.makeText(getContext(), "闹钟删除失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 处理POI搜索结果
    public void showPoiSearchResults(PoiResult poiResult) {
        if (mapController != null && poiResult != null) {
            // 清除地图上的现有标记
            mapController.clearAllPoiMarkers();

            // 获取POI列表
            List<PoiInfo> poiList = poiResult.getAllPoi();
            if (poiList != null && !poiList.isEmpty()) {
                // 在地图上添加POI标记
                for (PoiInfo poi : poiList) {
                    if (poi.location != null) {
                        mapController.showPoiMarker(poi);
                    }
                }
                // 缩放地图以显示所有POI
                mapController.zoomToFitAllPois(poiList);
            }
            Toast.makeText(getContext(), "已获取搜索结果", Toast.LENGTH_SHORT).show();
        }
    }

    // 显示添加闹钟对话框（带POI信息）
    private void showAddAlarmDialogWithPoi(final LatLng position, final String poiName, final String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_alarm, null);
        builder.setView(dialogView);
        
        // 初始化对话框控件
        EditText editAlarmName = dialogView.findViewById(R.id.editAlarmName);
        Slider editAlarmRadius = dialogView.findViewById(R.id.editAlarmRadius);
        TextView radiusValue = dialogView.findViewById(R.id.radiusValue);
        SwitchMaterial switchVibration = dialogView.findViewById(R.id.switchVibration);
        SwitchMaterial switchSound = dialogView.findViewById(R.id.switchSound);
        
        // 自动填充POI名称
        editAlarmName.setText(poiName);
        
        // 更新半径显示
        radiusValue.setText((int)editAlarmRadius.getValue() + "米");
        editAlarmRadius.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                radiusValue.setText((int)value + "米");
            }
        });
        
        builder.setTitle("添加空间闹钟")
            .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // 获取输入值
                    String title = editAlarmName.getText().toString().trim();
                    float radius = editAlarmRadius.getValue();
                    boolean isVibrate = switchVibration.isChecked();
                    boolean isRing = switchSound.isChecked();
                    
                    if (title.isEmpty()) {
                        Toast.makeText(getContext(), "请输入闹钟名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    // 创建闹钟
                    AlarmService alarmService = new AlarmService(getContext());
                    long alarmId = alarmService.createAlarmWithDetails(
                        title,
                        position.latitude,
                        position.longitude,
                        radius,
                        address,
                        "", // 可以添加默认消息
                        isVibrate,
                        isRing
                    );
                    
                    if (alarmId > 0) {
                        Toast.makeText(getContext(), "闹钟添加成功", Toast.LENGTH_SHORT).show();
                        // 刷新地图标记
                        mapController.loadAlarms();
                    } else {
                        Toast.makeText(getContext(), "闹钟添加失败", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onMapClick(LatLng latLng, String address) {
        // 存储获取到的地址
        this.lastClickedAddress = address != null ? address : "未知地址";
    }

    @Override
    public void onMarkerClick(Alarm alarm) {
        showEditAlarmDialog(alarm);
    }

    // 实现新增的POI标记点击方法
    @Override
    public void onPoiMarkerClick(LatLng latLng, String name, String address) {
        showAddAlarmDialogWithPoi(latLng, name, address);
    }

    @Override
    public void onLocationUpdate(LatLng currentLocation) {

    }
}
