package com.icloudaegis.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.kinsec.ktsdk.KTSDK;

//凯特管理员密码111111
public class KeySdkService {
    private static boolean initFlag = false;

    public boolean isInit() {
        return initFlag;
    }

    // String strSDCardDir = "/sdcard"; // 中兴
    // String strSDCardDir = "/mnt/external_sd"; // 宏基
    // String strSDCardDir = "/storage/extSdCard"; // 三星Galaxy Note2
    String strSDCardDir = Environment.getExternalStorageDirectory().toString();

    // String strSDCardDir = "/storage/sdcard1"; // 三星（Android5.0.2），小米
    // String strSDCardDir = "/sdcard"; // 模拟器

    static {
        // System.load("libstlport.so");
        // System.load("libFeiTian.so");
        // System.load("libshuttle_p11v220.so");
        System.loadLibrary("htpkcs11-android");
        System.loadLibrary("sdcardskf");
        System.loadLibrary("skf_fjkt");
        System.loadLibrary("KTSDK");
    }

    // 初始化
    @SuppressLint("NewApi")
    public PinResult initialize(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4的系统需要先修复O_DIRECT故障才能访问SDKey
            String strAPPFileDirs = null;
            context.getExternalFilesDirs(strAPPFileDirs);

            strSDCardDir += "/Android/data/" + context.getPackageName() + "/files";
        } else {
            strSDCardDir += "/wy";
        }
        // initialize
        String strLogFilePath = strSDCardDir + "/kslog.txt";
        KTSDK.KTSDK_SetLogFilePath(strLogFilePath);

        KTSDK.KTSDK_Initialize();
        KTSDK.KTCore_Initialize();

        int nRet = KTSDK.KTSDK_Device_Initialize(
                "/data/data/" + context.getPackageName() + "/lib", context.getPackageName());
        if (nRet != 0) {
            return new PinResult(-1, "初始化失败:"
                    + KTSDK.KTSDK_GetErrorString());
        }


        initFlag = true;
        return new PinResult(0, "初始化完成");

    }

    public PinResult loginPIN(String pin) {
        // open device
        int nRet = KTSDK.KTSDK_Device_Open();
        if (nRet != 0) {
            return new PinResult(-1, "打开设备出错:" + KTSDK.KTSDK_GetErrorString());
        }

        nRet = KTSDK.KTSDK_Device_Login(pin, true);
        if (nRet != 0) {
            String error = KTSDK.KTSDK_GetErrorString();
            int nPinCount = KTSDK.KTSDK_Device_GetRemainRetryCount(true);
            KTSDK.KTSDK_Device_Close();
            return new PinResult(-1, "登陆失败:" + error + " 剩余次数:" + nPinCount);
        }
        return new PinResult(0, "登陆成功");


    }

    public PinResult changePIN(String oldpin, String newpin) {
        // open device
        int nRet = KTSDK.KTSDK_Device_Open();
        if (nRet != 0) {
            return new PinResult(-1, "打开设备出错:" + KTSDK.KTSDK_GetErrorString());
        }

        // set pin
        nRet = KTSDK.KTSDK_Device_SetPin(oldpin, newpin, true);
        if (nRet != 0) {
            String error = KTSDK.KTSDK_GetErrorString();
            int nPinCount = KTSDK.KTSDK_Device_GetRemainRetryCount(true);
            KTSDK.KTSDK_Device_Close();
            return new PinResult(-1, "修改pin码出错:" + error
                    + ",请确认原始pin码是否正确\n 剩余次数:" + nPinCount);
        }

        // close device
        KTSDK.KTSDK_Device_Close();

        return new PinResult(0, "修改pin码成功");
    }

    // 解锁
    public PinResult unblockPIN(String adminPin, String userPin) {
        // open device
        int nRet = KTSDK.KTSDK_Device_Open();
        if (nRet != 0) {
            return new PinResult(-1, "打开设备出错:" + KTSDK.KTSDK_GetErrorString());
        }

        // unlock pin
        nRet = KTSDK.KTSDK_Device_UnlockUserPin(adminPin, userPin);
        if (nRet != 0) {
            String error = KTSDK.KTSDK_GetErrorString();
            int nPinCount = KTSDK.KTSDK_Device_GetRemainRetryCount(true);
            KTSDK.KTSDK_Device_Close();
            return new PinResult(-1, "解锁pin码出错:" + error + "\n 剩余次数:"
                    + nPinCount);
        }
        return new PinResult(0, "解锁pin码成功");
    }

    // 返回剩余次数
    public int ulRetrycount() {
        // open device
        int nRet = KTSDK.KTSDK_Device_Open();
        if (nRet != 0) {
            return -1;
        }
        int nPinCount = KTSDK.KTSDK_Device_GetRemainRetryCount(true);
        return nPinCount;
    }
}
