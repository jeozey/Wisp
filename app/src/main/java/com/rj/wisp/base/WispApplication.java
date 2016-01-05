package com.rj.wisp.base;

import android.app.Application;
import android.util.Log;

import com.rj.framework.CrashHandler;
import com.rj.wisp.bean.ResourceItem;

import java.util.HashMap;

public class WispApplication extends Application {
    private static final String TAG = WispApplication.class.getName();
    public static boolean isLogin = false;
    public static HashMap<String, ResourceItem> resourcesList = new HashMap<>();
    public static String cookies;
    private static WispApplication mInstance = null;

    public static WispApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onTerminate() {
        Log.e(TAG, "onTerminate");
        super.onTerminate();
    }

    @Override
    public void onCreate() {
        mInstance = this;
        // 奔溃日志捕捉
        CrashHandler catchHandler = CrashHandler.getInstance();
        catchHandler.init(getApplicationContext());


        super.onCreate();
    }

}
