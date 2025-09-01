package com.example.spacealarm.mapper.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.spacealarm.entity.Alarm;

import java.util.ArrayList;
import java.util.List;

public class AlarmDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "space_alarm.db";
    // 增加数据库版本号，用于升级
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_ALARMS = "alarms";
    
    // 新增：最后触发时间表
    private static final String TABLE_ALARM_TRIGGER_TIMES = "alarm_trigger_times";
    
    // 列名定义
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_RADIUS = "radius";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_IS_VIBRATE = "is_vibrate";
    private static final String COLUMN_IS_RING = "is_ring";
    private static final String COLUMN_IS_ENABLED = "is_enabled";
    private static final String COLUMN_CREATED_TIME = "created_time";
    
    // 新增：最后触发时间表列名
    private static final String COLUMN_ALARM_ID = "alarm_id";
    private static final String COLUMN_LAST_TRIGGER_TIME = "last_trigger_time";

    // 创建表的SQL语句
    private static final String CREATE_TABLE_ALARMS = "CREATE TABLE " + TABLE_ALARMS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_LATITUDE + " REAL NOT NULL, " +
            COLUMN_LONGITUDE + " REAL NOT NULL, " +
            COLUMN_RADIUS + " REAL NOT NULL, " +
            COLUMN_ADDRESS + " TEXT, " +
            COLUMN_MESSAGE + " TEXT, " +
            COLUMN_IS_VIBRATE + " INTEGER DEFAULT 1, " +
            COLUMN_IS_RING + " INTEGER DEFAULT 1, " +
            COLUMN_IS_ENABLED + " INTEGER DEFAULT 1, " +
            COLUMN_CREATED_TIME + " INTEGER NOT NULL" +
            ")";

    // 新增：创建最后触发时间表的SQL语句
    private static final String CREATE_TABLE_ALARM_TRIGGER_TIMES = "CREATE TABLE " + TABLE_ALARM_TRIGGER_TIMES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_ALARM_ID + " INTEGER NOT NULL, " +
            COLUMN_LAST_TRIGGER_TIME + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_ALARM_ID + ") REFERENCES " + TABLE_ALARMS + "(" + COLUMN_ID + ") ON DELETE CASCADE" +
            ")";

    public AlarmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALARMS);
        // 新增：创建最后触发时间表
        db.execSQL(CREATE_TABLE_ALARM_TRIGGER_TIMES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 新增：当数据库版本从1升级到2时，创建最后触发时间表
            db.execSQL(CREATE_TABLE_ALARM_TRIGGER_TIMES);
        }
    }

    // 插入闹钟
    public long insertAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, alarm.getTitle());
        values.put(COLUMN_LATITUDE, alarm.getLatitude());
        values.put(COLUMN_LONGITUDE, alarm.getLongitude());
        values.put(COLUMN_RADIUS, alarm.getRadius());
        values.put(COLUMN_ADDRESS, alarm.getAddress());
        values.put(COLUMN_MESSAGE, alarm.getMessage());
        values.put(COLUMN_IS_VIBRATE, alarm.isVibrate() ? 1 : 0);
        values.put(COLUMN_IS_RING, alarm.isRing() ? 1 : 0);
        values.put(COLUMN_IS_ENABLED, alarm.isEnabled() ? 1 : 0);
        values.put(COLUMN_CREATED_TIME, alarm.getCreatedTime());

        long id = db.insert(TABLE_ALARMS, null, values);
        db.close();
        return id;
    }

    // 获取所有闹钟
    public List<Alarm> getAllAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ALARMS + " ORDER BY " + COLUMN_CREATED_TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                alarm.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                alarm.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
                alarm.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
                alarm.setRadius(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RADIUS)));
                alarm.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                alarm.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                alarm.setVibrate(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VIBRATE)) == 1);
                alarm.setRing(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_RING)) == 1);
                alarm.setEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ENABLED)) == 1);
                alarm.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIME)));

                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return alarms;
    }

    // 获取启用的闹钟
    public List<Alarm> getEnabledAlarms() {
        List<Alarm> alarms = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ALARMS +
                " WHERE " + COLUMN_IS_ENABLED + " = 1" +
                " ORDER BY " + COLUMN_CREATED_TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                alarm.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                alarm.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
                alarm.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
                alarm.setRadius(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RADIUS)));
                alarm.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                alarm.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                alarm.setVibrate(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VIBRATE)) == 1);
                alarm.setRing(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_RING)) == 1);
                alarm.setEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ENABLED)) == 1);
                alarm.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIME)));

                alarms.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return alarms;
    }

    // 更新闹钟
    public int updateAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, alarm.getTitle());
        values.put(COLUMN_LATITUDE, alarm.getLatitude());
        values.put(COLUMN_LONGITUDE, alarm.getLongitude());
        values.put(COLUMN_RADIUS, alarm.getRadius());
        values.put(COLUMN_ADDRESS, alarm.getAddress());
        values.put(COLUMN_MESSAGE, alarm.getMessage());
        values.put(COLUMN_IS_VIBRATE, alarm.isVibrate() ? 1 : 0);
        values.put(COLUMN_IS_RING, alarm.isRing() ? 1 : 0);
        values.put(COLUMN_IS_ENABLED, alarm.isEnabled() ? 1 : 0);

        int rows = db.update(TABLE_ALARMS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(alarm.getId())});
        db.close();
        return rows;
    }

    // 删除闹钟
    public int deleteAlarm(long alarmId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_ALARMS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(alarmId)});
        db.close();
        return rows;
    }

    // 根据ID获取闹钟
    public Alarm getAlarmById(long alarmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ALARMS, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(alarmId)}, null, null, null);

        Alarm alarm = null;
        if (cursor.moveToFirst()) {
            alarm = new Alarm();
            alarm.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            alarm.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
            alarm.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
            alarm.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
            alarm.setRadius(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_RADIUS)));
            alarm.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
            alarm.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
            alarm.setVibrate(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VIBRATE)) == 1);
            alarm.setRing(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_RING)) == 1);
            alarm.setEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ENABLED)) == 1);
            alarm.setCreatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIME)));
        }
        cursor.close();
        db.close();
        return alarm;
    }

    // 获取闹钟数量
    public int getAlarmCount() {
        String countQuery = "SELECT * FROM " + TABLE_ALARMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    // 新增：更新闹钟最后触发时间
    public void updateAlarmLastTriggerTime(long alarmId, long triggerTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // 先检查是否已存在该闹钟的触发时间记录
        Cursor cursor = db.query(TABLE_ALARM_TRIGGER_TIMES, null, 
                COLUMN_ALARM_ID + " = ?", new String[]{String.valueOf(alarmId)}, 
                null, null, null);
        
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_TRIGGER_TIME, triggerTime);
        
        if (cursor.moveToFirst()) {
            // 已存在记录，更新
            db.update(TABLE_ALARM_TRIGGER_TIMES, values, 
                    COLUMN_ALARM_ID + " = ?", new String[]{String.valueOf(alarmId)});
        } else {
            // 不存在记录，插入
            values.put(COLUMN_ALARM_ID, alarmId);
            db.insert(TABLE_ALARM_TRIGGER_TIMES, null, values);
        }
        
        cursor.close();
        db.close();
    }
    
    // 新增：获取闹钟最后触发时间
    public Long getAlarmLastTriggerTime(long alarmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ALARM_TRIGGER_TIMES, new String[]{COLUMN_LAST_TRIGGER_TIME}, 
                COLUMN_ALARM_ID + " = ?", new String[]{String.valueOf(alarmId)}, 
                null, null, null);
        
        Long lastTriggerTime = null;
        if (cursor.moveToFirst()) {
            lastTriggerTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_TRIGGER_TIME));
        }
        
        cursor.close();
        db.close();
        return lastTriggerTime;
    }
}