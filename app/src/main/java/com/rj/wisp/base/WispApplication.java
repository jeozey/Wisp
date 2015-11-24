package com.rj.wisp.base;

import android.app.Application;
import android.util.Log;

import com.rj.framework.CrashHandler;

public class WispApplication extends Application {
    private static final String TAG = WispApplication.class.getName();
    public static final long NOW = System.currentTimeMillis();
    public static boolean isLogin = false;
    public static int OpenType = 0;
    public static final int OPENTYPE_WPS = 1;
    public static final int OPENTYPE_ANNOTATION = 2;
    public static final int OPENTYPE_PDF = 3;
    // public static final int OPENTYPE_PDF_LOG = 4;

    public static String tabMsg = "";// 平板pdf阅读器用到
    public static String cookies = "";
    public static String docType = "";
    public static String UserName = "";
    public static String PDFName = "";
    public static String FileName = "";//用于适配那边给的名称
    public static String PDFLogName = "";


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
