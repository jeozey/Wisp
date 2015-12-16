package com.rj.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;


public class AppSystemTool {
    private static final String TAG = AppSystemTool.class.getName();

    public static void clearWebViewCookie(Context context) {
        try {
            CookieSyncManager.createInstance(context);
            CookieManager.getInstance().removeAllCookie();
            context.deleteDatabase("webView.db");
            context.deleteDatabase("webviewCookiesChromiumPrivate.db");
            context.deleteDatabase("webviewCookiesChromium.db");
            context.deleteDatabase("webViewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public static int getSDKVersionNumber() {
        int sdkVersion;
        try {
            sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            sdkVersion = 0;
        }
        return sdkVersion;
    }

    public static void restartApp(Activity currentActitity, Class<? extends Activity> clz) {
        /*********************************** 重新启动开始 ***************************************/
        try {
            Log.e(TAG, "程序重启");
            Intent mainIntent = new Intent(currentActitity,
                    clz);
            mainIntent.putExtra("ispin", DB.IS_PIN);
            mainIntent.putExtra("isulan", DB.IS_ULAN);
            currentActitity.startActivity(mainIntent);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // d.杀进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        /*********************************** 重新启动结束 ***************************************/
    }

}
