package com.example.spacealarm.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExceptionUtil {
    private static final String TAG = "ExceptionUtil";
    private static final String LOG_FILE_NAME = "app_exception.log";

    // 使用Toast显示错误信息
    public static void showToast(Context context, String tag, String message) {
        if (context != null) {
            String errorMsg = tag + ": " + message;
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
            Log.e(tag, message);
        }
    }

    // 记录错误日志到本地文件
    public static void logToFile(Context context, String tag, String message) {
        try {
            File logFile = new File(context.getExternalFilesDir(null), LOG_FILE_NAME);
            FileWriter writer = new FileWriter(logFile, true);

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String logEntry = String.format("[%s] [%s] %s\n", timeStamp, tag, message);

            writer.append(logEntry);
            writer.flush();
            writer.close();

            Log.e(tag, message);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write exception log to file: " + e.getMessage());
        }
    }

    // 同时使用Toast显示和记录到文件
    public static void handleException(Context context, String tag, String message) {
        showToast(context, tag, message);
        logToFile(context, tag, message);
    }
}