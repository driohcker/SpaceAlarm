package com.example.spacealarm.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 语音朗读管理器，负责处理应用中的文本到语音转换功能
 */
public class TextToSpeechManager {
    private static final String TAG = "TextToSpeechManager";
    private static TextToSpeechManager instance;
    private final Context context;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private boolean isInitializing = false;
    // 添加重试计数器，防止无限重试
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private static final int MAX_RETRY_COUNT = 5;
    // 初始化超时时间（毫秒）
    private static final int INIT_TIMEOUT_MS = 5000;
    // 初始化超时标记
    private boolean initTimedOut = false;

    private TextToSpeechManager(Context context) {
        this.context = context.getApplicationContext();
        initTextToSpeech();
    }

    /**
     * 获取单例实例
     */
    public static synchronized TextToSpeechManager getInstance(Context context) {
        if (instance == null) {
            instance = new TextToSpeechManager(context);
        }
        return instance;
    }

    /**
     * 初始化TextToSpeech引擎，确保使用系统TTS
     */
    private void initTextToSpeech() {
        if (isInitializing) {
            Log.d(TAG, "TextToSpeech已经在初始化中");
            return;
        }
        
        isInitializing = true;
        isInitialized = false;
        initTimedOut = false;
        retryCount.set(0);
        
        Log.d(TAG, "开始初始化系统TextToSpeech引擎");
        
        // 使用系统默认的TTS引擎
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                isInitializing = false;
                
                Log.d(TAG, "TextToSpeech初始化回调，status: " + status);
                
                if (status == TextToSpeech.SUCCESS) {
                    // 尝试多种中文语言变体
                    boolean languageSet = false;
                    
                    // 尝试简体中文（中国大陆）
                    if (!languageSet) {
                        int result = textToSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE);
                        if (result == TextToSpeech.LANG_AVAILABLE) {
                            Log.d(TAG, "TextToSpeech语言设置为简体中文（中国大陆）");
                            languageSet = true;
                        } else {
                            Log.w(TAG, "简体中文（中国大陆）不可用，错误码: " + result);
                        }
                    }
                    
                    // 尝试繁体中文（台湾）
                    if (!languageSet) {
                        int result = textToSpeech.setLanguage(Locale.TRADITIONAL_CHINESE);
                        if (result == TextToSpeech.LANG_AVAILABLE) {
                            Log.d(TAG, "TextToSpeech语言设置为繁体中文（台湾）");
                            languageSet = true;
                        } else {
                            Log.w(TAG, "繁体中文（台湾）不可用，错误码: " + result);
                        }
                    }
                    
                    // 尝试中文（香港）
                    if (!languageSet) {
                        Locale locale = new Locale("zh", "HK");
                        int result = textToSpeech.setLanguage(locale);
                        if (result == TextToSpeech.LANG_AVAILABLE) {
                            Log.d(TAG, "TextToSpeech语言设置为中文（香港）");
                            languageSet = true;
                        } else {
                            Log.w(TAG, "中文（香港）不可用，错误码: " + result);
                        }
                    }
                    
                    // 最后尝试系统默认语言
                    if (!languageSet) {
                        Locale defaultLocale = Locale.getDefault();
                        textToSpeech.setLanguage(defaultLocale);
                        Log.d(TAG, "TextToSpeech语言设置为系统默认语言: " + defaultLocale.getDisplayName());
                        languageSet = true;
                    }
                    
                    // 设置语音朗读速度和音调
                    textToSpeech.setSpeechRate(1.0f); // 默认速度
                    textToSpeech.setPitch(1.0f);     // 默认音调
                    
                    isInitialized = true;
                    Log.d(TAG, "TextToSpeech初始化成功");
                } else {
                    Log.e(TAG, "TextToSpeech初始化失败: status=" + status + ", 错误描述: " + getStatusDescription(status));
                    isInitialized = false;
                }
            }
        });
        
        // 设置初始化超时机制
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInitializing && !isInitialized) {
                    initTimedOut = true;
                    isInitializing = false;
                    Log.e(TAG, "TextToSpeech初始化超时（" + INIT_TIMEOUT_MS + "ms）");
                    
                    // 建议用户安装TTS引擎
                    promptUserToInstallTTS();
                }
            }
        }, INIT_TIMEOUT_MS);
    }
    
    /**
     * 获取状态码描述
     */
    private String getStatusDescription(int status) {
        switch (status) {
            case TextToSpeech.SUCCESS:
                return "SUCCESS";
            case TextToSpeech.ERROR:
                return "ERROR - 通用错误";
            case TextToSpeech.ERROR_INVALID_REQUEST:
                return "ERROR_INVALID_REQUEST - 请求无效";
            case TextToSpeech.ERROR_NOT_INSTALLED_YET:
                return "ERROR_NOT_INSTALLED_YET - TTS引擎尚未安装";
            case TextToSpeech.ERROR_NETWORK:
                return "ERROR_NETWORK - 网络错误";
            case TextToSpeech.ERROR_NETWORK_TIMEOUT:
                return "ERROR_NETWORK_TIMEOUT - 网络超时";
            case TextToSpeech.ERROR_SYNTHESIS:
                return "ERROR_SYNTHESIS - 合成错误";
            default:
                return "未知错误码: " + status;
        }
    }
    
    /**
     * 提示用户安装TTS引擎
     */
    private void promptUserToInstallTTS() {
        try {
            Intent installIntent = new Intent();
            // 首先尝试标准的TTS安装Intent
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            // 检查是否有Activity可以处理这个Intent
            if (installIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(installIntent);
                Log.d(TAG, "已启动TTS引擎安装界面");
            } else {
                // 如果没有处理标准Intent的Activity，尝试打开系统设置的语言和输入法页面
                Log.w(TAG, "没有找到处理TTS安装的Activity，尝试打开系统设置");
                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                Log.d(TAG, "已打开系统语言和输入法设置页面");
            }
        } catch (Exception e) {
            Log.e(TAG, "启动TTS引擎安装界面失败: " + e.getMessage());
            // 可以考虑添加一个Toast提示用户手动安装TTS引擎
        }
    }

    /**
     * 朗读指定文本
     * @param text 要朗读的文本内容
     */
    public void speak(String text) {
        if (textToSpeech == null) {
            Log.e(TAG, "TextToSpeech未初始化，重新初始化");
            initTextToSpeech();
            return;
        }

        if (initTimedOut) {
            Log.e(TAG, "TextToSpeech初始化已超时，无法朗读文本");
            return;
        }

        if (isInitialized) {
            // 重置重试计数器
            retryCount.set(0);
            
            // 停止当前正在朗读的内容
            textToSpeech.stop();
            // 开始朗读新内容
            int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "开始朗读: " + text);
            } else {
                Log.e(TAG, "朗读失败，错误码: " + result);
            }
        } else {
            int currentRetry = retryCount.incrementAndGet();
            if (currentRetry > MAX_RETRY_COUNT) {
                Log.e(TAG, "TextToSpeech初始化失败，已达到最大重试次数（" + MAX_RETRY_COUNT + "）");
                // 如果重试次数过多，提示用户安装TTS引擎
                promptUserToInstallTTS();
                return;
            }
            
            Log.d(TAG, "TextToSpeech正在初始化中（第" + currentRetry + "次尝试），稍后朗读");
            
            // 如果还未初始化完成，延迟一段时间后重试，每次重试增加延迟时间
            int delay = 500 * currentRetry;
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    speak(text);
                }
            }, delay);
        }
    }

    /**
     * 释放TextToSpeech资源
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            isInitialized = false;
            isInitializing = false;
            initTimedOut = false;
            Log.d(TAG, "TextToSpeech资源已释放");
        }
    }

    /**
     * 检查TextToSpeech是否可用
     */
    public boolean isAvailable() {
        return textToSpeech != null && isInitialized && !initTimedOut;
    }

    /**
     * 设置语音朗读速度
     * @param rate 速度值，1.0为默认速度
     */
    public void setSpeechRate(float rate) {
        if (textToSpeech != null && isInitialized) {
            textToSpeech.setSpeechRate(rate);
        }
    }

    /**
     * 设置语音音调
     * @param pitch 音调值，1.0为默认音调
     */
    public void setPitch(float pitch) {
        if (textToSpeech != null && isInitialized) {
            textToSpeech.setPitch(pitch);
        }
    }

    /**
     * 检查系统是否安装了可用的TTS引擎
     */
    public boolean isSystemTTSAvailable() {
        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(ttsIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }
}