package com.example.spacealarm.activity.widget;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.example.spacealarm.R;
import com.example.spacealarm.fragment.AlarmFragment;
import com.example.spacealarm.fragment.MapFragment;
import com.example.spacealarm.fragment.SettingsFragment;

// 添加必要的导入
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.spacealarm.service.BaiduLocationService;

public class CustomToolbarManager {
    private static Toolbar toolbar;
    private static View titleLayout;
    private static View searchLayout;
    private static EditText searchEditText;
    private static PoiSearch mPoiSearch;
    private static MapFragment mapFragment;

    // 初始化Toolbar管理器
    public static void setup(Activity activity) {
        toolbar = activity.findViewById(R.id.toolbar);
        titleLayout = activity.findViewById(R.id.toolbar_title_layout);
        searchLayout = activity.findViewById(R.id.toolbar_search_layout);
        searchEditText = activity.findViewById(R.id.search_edit_text);

        // 初始化POI搜索
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult == null || poiResult.error != PoiResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(activity, "未找到搜索结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 将搜索结果传递给MapFragment处理
                if (mapFragment != null) {
                    mapFragment.showPoiSearchResults(poiResult);
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
                // 不需要处理详情
            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                // 不需要处理室内结果
            }

            @Override
            public void onGetPoiDetailResult(com.baidu.mapapi.search.poi.PoiDetailResult poiDetailResult) {
                // 旧版回调，已废弃
            }
        });

        // 设置搜索按钮点击事件
        ImageView searchButton = activity.findViewById(R.id.search_button);
        if (searchButton != null) {
            searchButton.setOnClickListener(v -> performSearch(activity));
        }
    }

    // 更新MapFragment引用
    public static void setMapFragment(MapFragment fragment) {
        mapFragment = fragment;
    }

    // 执行搜索操作
    private static void performSearch(Activity activity) {
        if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) {
            String keyword = searchEditText.getText().toString();
            
            // 获取用户当前城市
            String city = null;
            try {
                BaiduLocationService locationService = BaiduLocationService.getInstance(activity);
                city = locationService.getCurrentCity();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 如果无法获取当前城市，使用默认城市"北京"
            if (city == null || city.isEmpty()) {
                city = "北京";
            }
            
            // 使用当前城市进行搜索
            mPoiSearch.searchInCity(new PoiCitySearchOption()
                    .city(city)
                    .keyword(keyword)
                    .pageNum(0));
        } else {
            Toast.makeText(activity, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
        }
    }

    // 在适当的生命周期方法中释放资源
    public static void onDestroy() {
        if (mPoiSearch != null) {
            mPoiSearch.destroy();
        }
    }

    // 根据Fragment类型切换Toolbar样式
    public static void switchToolbarForFragment(Class<?> fragmentClass) {
        if (fragmentClass == AlarmFragment.class || fragmentClass == SettingsFragment.class) {
            showTitleMode();
        } else if (fragmentClass == MapFragment.class) {
            showSearchMode();
        }
    }

    // 显示标题模式
    private static void showTitleMode() {
        if (titleLayout != null && searchLayout != null) {
            titleLayout.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.GONE);
        }
    }

    // 显示搜索模式
    private static void showSearchMode() {
        if (titleLayout != null && searchLayout != null) {
            titleLayout.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
        }
    }
}