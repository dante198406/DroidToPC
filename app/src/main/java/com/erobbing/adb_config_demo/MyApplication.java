package com.erobbing.adb_config_demo;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context mContext = null;
    public static Context getContext(){
        return mContext;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
