package com.example.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 数据持久化工具类
 */
public class SharedPreferencesUtil {

    private static String FILE_NAME = "SharedPreferences";

    public static void saveStringValue(Context ctx, String key, String value) {
        saveValue(ctx, key, value);
    }

    public static String getStringValue(Context ctx, String key) {
        Object value = getValue(ctx, key, "");
        if (value != null && (value instanceof String)) {
            return (String) value;
        }
        return "";
    }

    public static void saveLongValue(Context ctx, String key, long value) {
        saveValue(ctx, key, value);
    }

    public static long getLongValue(Context ctx, String key) {
        Object value = getValue(ctx, key, 0L);
        if (value != null && (value instanceof Long)) {
            return (Long) value;
        }
        return 0L;
    }

    /**
     * 将数据保存到本地
     *
     * @param ctx   Context对象
     * @param key   键对象
     * @param value 值对象
     */
    private static void saveValue(Context ctx, String key, Object value) {
        try {
            SharedPreferences sharedPreferences = ctx.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            }
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取本地数据
     *
     * @param ctx          Context对象
     * @param key          键对象
     * @param defaultValue 默认值
     * @return 值对象
     */
    private static Object getValue(Context ctx, String key, Object defaultValue) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (defaultValue instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof String) {
            return sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultValue);
        }
        return null;
    }

}
