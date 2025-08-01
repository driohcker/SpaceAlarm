package com.example.spacealarm.activity.widget;

import android.app.Activity;
import android.util.DisplayMetrics;
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

// 添加必要的导入
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.spacealarm.R;
import java.util.List;

public class CustomToolbarManager {
    private static Toolbar toolbar;
    private static View titleLayout;
    private static View searchLayout;
    private static EditText searchEditText;
    private static PoiSearch mPoiSearch;
    private static MapFragment mapFragment;
    private static SearchHistoryManager searchHistoryManager;
    private static PopupWindow searchHistoryPopup;
    private static SearchHistoryAdapter searchHistoryAdapter;

    // 初始化Toolbar管理器
    public static void setup(Activity activity) {
        toolbar = activity.findViewById(R.id.toolbar);
        titleLayout = activity.findViewById(R.id.toolbar_title_layout);
        searchLayout = activity.findViewById(R.id.toolbar_search_layout);
        searchEditText = activity.findViewById(R.id.search_edit_text);

        // 初始化搜索历史管理器
        searchHistoryManager = SearchHistoryManager.getInstance(activity);

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
            searchButton.setOnClickListener(v -> {
                String keyword = searchEditText.getText().toString();
                if (!keyword.isEmpty()) {
                    // 添加到搜索历史
                    searchHistoryManager.addSearchHistory(keyword);
                    performSearch(activity);
                    // 隐藏搜索历史弹窗
                    hideSearchHistoryPopup();
                }else{
                    Toast.makeText(activity, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 添加搜索历史按钮点击事件
        ImageView historyButton = activity.findViewById(R.id.history_button);
        if (historyButton != null) {
            historyButton.setOnClickListener(v -> showSearchHistory(activity));
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
            String city = null;

            // 1. 尝试从关键词中提取城市名称
            city = extractCityFromKeyword(keyword);

            // 2. 如果关键词中没有明确的城市名称，则使用当前城市
            if (city == null || city.isEmpty()) {
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
            } else {
                // 3. 如果关键词中包含城市名称，提取纯关键词
                keyword = keyword.replaceAll(city, "").trim();
                // 处理可能的标点符号
                if (keyword.startsWith("市")) {
                    keyword = keyword.substring(1).trim();
                }
            }

            // 4. 使用确定的城市和关键词进行搜索
            mPoiSearch.searchInCity(new PoiCitySearchOption()
                    .city(city)
                    .keyword(keyword)
                    .pageNum(0));
        } else {
            Toast.makeText(activity, "请输入搜索关键词", Toast.LENGTH_SHORT).show();
        }
    }

    // 辅助方法：从关键词中提取城市名称
    private static String extractCityFromKeyword(String keyword) {
        // 这里使用百度地图SDK的地理编码API来解析关键词
        // 简单实现：检查关键词是否包含常见城市后缀
        String[] citySuffixes = {"市", "省", "自治区", "特别行政区"};
        
        // 查找关键词中是否包含城市后缀
        for (String suffix : citySuffixes) {
            int index = keyword.indexOf(suffix);
            if (index > 0) {
                // 提取城市名称（假设城市名称至少有2个字符）
                if (index >= 1) {
                    return keyword.substring(0, index + suffix.length());
                }
            }
        }
        
        // 检查常见城市的简称或别名
        String[][] cityAlias = {
            {"北京", "北京市"},
            {"上海", "上海市"},
            {"广州", "广州市"},
            {"深圳", "深圳市"},
            {"杭州", "杭州市"},
            {"郴州", "郴州市"},
            {"衡阳", "衡阳市"},
            {"长沙", "长沙市"}
            // 可以添加更多城市别名
        };
        
        for (String[] alias : cityAlias) {
            if (keyword.contains(alias[0]) && !keyword.contains(alias[1])) {
                return alias[1];
            }
        }
        
        return null;
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

    // 新增方法：显示搜索历史
    private static void showSearchHistory(Activity activity) {
        if (searchHistoryPopup == null) {
            View view = LayoutInflater.from(activity).inflate(R.layout.search_history_layout, null);
            RecyclerView recyclerView = view.findViewById(R.id.search_history_recycler);
            TextView clearHistory = view.findViewById(R.id.clear_history);

            // 设置RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            List<String> historyList = searchHistoryManager.getSearchHistory();
            searchHistoryAdapter = new SearchHistoryAdapter(historyList);
            recyclerView.setAdapter(searchHistoryAdapter);

            // 设置搜索历史项点击事件
            searchHistoryAdapter.setOnHistoryItemClickListener(keyword -> {
                searchEditText.setText(keyword);
                searchHistoryPopup.dismiss();
                performSearch(activity);
            });

            // 设置删除按钮点击事件
            searchHistoryAdapter.setOnDeleteClickListener(keyword -> {
                searchHistoryManager.deleteSearchHistory(keyword);
                searchHistoryAdapter = new SearchHistoryAdapter(searchHistoryManager.getSearchHistory());
                recyclerView.setAdapter(searchHistoryAdapter);
                // 重新设置监听器
                searchHistoryAdapter.setOnHistoryItemClickListener(searchHistoryAdapter.getOnHistoryItemClickListener());
                searchHistoryAdapter.setOnDeleteClickListener(searchHistoryAdapter.getOnDeleteClickListener());
            });

            // 设置清除全部历史点击事件
            clearHistory.setOnClickListener(v -> {
                searchHistoryManager.clearSearchHistory();
                searchHistoryAdapter = new SearchHistoryAdapter(searchHistoryManager.getSearchHistory());
                recyclerView.setAdapter(searchHistoryAdapter);
                // 重新设置监听器
                searchHistoryAdapter.setOnHistoryItemClickListener(searchHistoryAdapter.getOnHistoryItemClickListener());
                searchHistoryAdapter.setOnDeleteClickListener(searchHistoryAdapter.getOnDeleteClickListener());
            });

            // 创建PopupWindow
            // 计算弹窗宽度，比屏幕宽度小32dp（左右各16dp边距）
            int margin = activity.getResources().getDimensionPixelSize(R.dimen.search_history_margin);
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            int width = screenWidth - 2 * margin;
            searchHistoryPopup = new PopupWindow(view, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            searchHistoryPopup.setOutsideTouchable(true);
            searchHistoryPopup.setFocusable(true);
            searchHistoryPopup.setBackgroundDrawable(activity.getResources().getDrawable(android.R.color.transparent));
        } else {
            // 更新搜索历史数据
            List<String> historyList = searchHistoryManager.getSearchHistory();
            searchHistoryAdapter = new SearchHistoryAdapter(historyList);
            RecyclerView recyclerView = (RecyclerView) searchHistoryPopup.getContentView().findViewById(R.id.search_history_recycler);
            recyclerView.setAdapter(searchHistoryAdapter);
            // 重新设置监听器
            searchHistoryAdapter.setOnHistoryItemClickListener(keyword -> {
                searchEditText.setText(keyword);
                searchHistoryPopup.dismiss();
            });
            searchHistoryAdapter.setOnDeleteClickListener(keyword -> {
                searchHistoryManager.deleteSearchHistory(keyword);
                searchHistoryAdapter = new SearchHistoryAdapter(searchHistoryManager.getSearchHistory());
                recyclerView.setAdapter(searchHistoryAdapter);
                // 重新设置监听器
                searchHistoryAdapter.setOnHistoryItemClickListener(searchHistoryAdapter.getOnHistoryItemClickListener());
                searchHistoryAdapter.setOnDeleteClickListener(searchHistoryAdapter.getOnDeleteClickListener());
            });
        }

        // 显示PopupWindow，设置左右边距
        int margin = activity.getResources().getDimensionPixelSize(R.dimen.search_history_margin);
        searchHistoryPopup.showAsDropDown(toolbar, 0, 10);
    }

    // 新增方法：隐藏搜索历史弹窗
    public static void hideSearchHistoryPopup() {
        if (searchHistoryPopup != null && searchHistoryPopup.isShowing()) {
            searchHistoryPopup.dismiss();
        }
    }
}