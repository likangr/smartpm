package com.likangr.smartpm.demo;

import android.app.Application;

import com.likangr.smartpm.lib.SmartPM;

/**
 * @author likangren
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SmartPM.initCore(this);
    }
}
