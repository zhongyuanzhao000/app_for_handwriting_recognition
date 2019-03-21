package com.example.application;

import android.app.Application;
import android.content.Context;

import com.example.pojo.FishInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于保存全局变量
 */
public class MyApplication extends Application {
    private static Context context;

    public static List<FishInfo> recordList=new ArrayList<>();

    public static List<FishInfo> resList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
