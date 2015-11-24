package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;

import java.io.InputStream;

/**
 * 作者：志文 on 2015/11/18 0018 15:53
 * 邮箱：594485991@qq.com
 */
public class InitUtil {
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
