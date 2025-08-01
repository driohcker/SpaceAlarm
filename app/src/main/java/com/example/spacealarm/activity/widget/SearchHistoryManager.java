package com.example.spacealarm.activity.widget;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.JsonSyntaxException;

public class SearchHistoryManager {

    private static final String PREF_NAME = "search_history";
    private static final String KEY_HISTORY = "history_list";
    private static SearchHistoryManager sInstance;
    private SharedPreferences mSharedPreferences;
    private Gson mGson;

    private SearchHistoryManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mGson = new Gson();
    }

    public static synchronized SearchHistoryManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SearchHistoryManager(context.getApplicationContext());
        }
        return sInstance;
    }

    // 添加搜索历史
    public void addSearchHistory(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        List<String> historyList = getSearchHistory();
        // 避免重复添加
        Set<String> historySet = new HashSet<>(historyList);
        historySet.add(keyword);
        historyList = new ArrayList<>(historySet);

        // 保存到SharedPreferences
        String json = mGson.toJson(historyList);
        mSharedPreferences.edit().putString(KEY_HISTORY, json).apply();
    }

    // 获取搜索历史
    public List<String> getSearchHistory() {
        String json = mSharedPreferences.getString(KEY_HISTORY, null);
        if (json == null) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<String>>() {}.getType();
            return mGson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            // 如果解析失败，清除错误数据并返回空列表
            mSharedPreferences.edit().remove(KEY_HISTORY).apply();
            return new ArrayList<>();
        }
    }

    // 删除单条搜索历史
    public void deleteSearchHistory(String keyword) {
        List<String> historyList = getSearchHistory();
        historyList.remove(keyword);

        String json = mGson.toJson(historyList);
        mSharedPreferences.edit().putString(KEY_HISTORY, json).apply();
    }

    // 清除所有搜索历史
    public void clearSearchHistory() {
        mSharedPreferences.edit().remove(KEY_HISTORY).apply();
    }
}