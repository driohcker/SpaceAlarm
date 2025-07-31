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
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_ALARMS = "alarms";

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

    public AlarmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ALARMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
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
}