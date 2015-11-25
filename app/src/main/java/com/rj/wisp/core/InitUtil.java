package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;

import com.rj.util.EnvironmentUtil;

import java.io.File;
import java.io.InputStream;

/**
 * 作者：志文 on 2015/11/18 0018 15:53
 * 邮箱：594485991@qq.com
 */
public class InitUtil {
    public static boolean initFilePath(Context context) {
        try {
            DB.SDCARD_PATH = EnvironmentUtil.getExternalSdCardPath();
            //----------/mnt/sdcard/Android/data/pkg/files
            DB.EXTERNAL_FILE_PATH = context.getExternalFilesDir(null).getAbsolutePath();
            //----------/mnt/sdcard/Android/data/pkg/cache
            DB.CACHE_FILE_PATH = context.getExternalCacheDir().getAbsolutePath();

            DB.RESOURCE_PATH = DB.SDCARD_PATH + "/wisp/resources/"
                    + DB.SECURITY_HOST + "_" + DB.SECURITY_PORT + File.separator;
            DB.DOWN_FILE_PATH = DB.SDCARD_PATH + "/wisp/file/"
                    + DB.SECURITY_HOST + "_" + DB.SECURITY_PORT + File.separator;

            File file = new File(DB.RESOURCE_PATH);
            if (!file.isDirectory() && !file.exists()) {
                file.mkdirs();
            }
            file = new File(DB.DOWN_FILE_PATH);
            if (!file.isDirectory() && !file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean initHttpServer(Context context, Handler handler) {
        // 启动8011 server
        try {
            WispCore.getWISPSO().StartService(handler, context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean initSSLContext(Context context, Handler handler) {
        try {
            InputStream kclient_is = null; // 客户端证书

            InputStream tclient_is = null; // 客户端受信任的服务器证书列表


            try {
                kclient_is = context.getResources().openRawResource(
                        context.getResources().getIdentifier("kclient", "raw",
                                context.getPackageName()));
            } catch (Exception e) {
                return false;
            }

            // 客户端受信任的服务器证书列表
            try {
                tclient_is = context.getResources().openRawResource(
                        context.getResources().getIdentifier("tclient", "raw",
                                context.getPackageName()));
            } catch (Exception e) {
                return false;
            }

            // init sslContext
            try {
                WispCore.getWISPSO().SSLServerInit(kclient_is, tclient_is);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
