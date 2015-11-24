package com.rj.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class AndroidTool {

    public static String getVersionCode(Context context) {
        String versionName = "";
        try {
            PackageInfo pinfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_CONFIGURATIONS);
            versionName = pinfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getAvailMemory(Activity activity) {// 获取android当前可用内存大小
        //
        getTotalMemory(activity);

        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内�? 

        return Formatter.formatFileSize(activity.getBaseContext(), mi.availMem);// 将获取的内存大小规格�? 
    }

    public static String getTotalMemory(Activity activity) {
        String str1 = "/proc/meminfo";// 系统内存信息文件  
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大�? 

            arrayOfString = str2.split("//s+");
            for (String num : arrayOfString) {
                //Log.i(str2, num + "/t");  
            }
//            System.out.println("总内存：" + str2);
//            initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘�?024转换为Byte  
//            localBufferedReader.close();  

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(activity.getBaseContext(), 0);// Byte转换为KB或�?MB，内存大小规格化  
    }
}
