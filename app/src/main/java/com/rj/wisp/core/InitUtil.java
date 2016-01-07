package com.rj.wisp.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.rj.framework.DB;
import com.rj.framework.SharedPreferencesUtil;
import com.rj.util.AndroidTool;
import com.rj.util.SystemUtil;
import com.rj.wisp.BuildConfig;

import java.io.File;
import java.io.InputStream;

/**
 * 作者：志文 on 2015/11/18 0018 15:53
 * 邮箱：594485991@qq.com
 */
public class InitUtil {
    private static final String TAG = InitUtil.class.getName();

    public static boolean initDB(Context context) {
        try {
            DB.SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
            DB.isPhone = !SystemUtil.isPad(context);
            Log.e(TAG, "DB.isPhone:" + DB.isPhone);
//            DB.IS_GZIP = BuildConfig.IS_GZIP;
            DB.HTTPSERVER_HOST = BuildConfig.HTTPSERVER_HOST;
            DB.HTTPSERVER_PORT = BuildConfig.HTTPSERVER_PORT;
            Log.e(TAG, "DB.HTTPSERVER_PORT:" + DB.HTTPSERVER_PORT);
            DB.SECURITY_HOST = BuildConfig.SECURITY_HOST;
            DB.SECURITY_PORT = BuildConfig.SECURITY_PORT;
            DB.APP_CODE = BuildConfig.APP_CODE;
            DB.IS_PIN = BuildConfig.IS_PIN;

            DB.USE_VPN = BuildConfig.USE_VPN;
            DB.VPN_HOST = BuildConfig.VPN_HOST;
            DB.VPN_PORT = BuildConfig.VPN_PORT;
            DB.VPN_USER = BuildConfig.VPN_USER;
            DB.VPN_PASS = BuildConfig.VPN_PASS;


            String version = AndroidTool.getVersionCode(context);
            DB.USER_AGENT = "; RJ-WISP-Client (IMEI:" + DB.IMEI + " ;IMSI:" + DB.IMSI + "; KEYID:" + DB.KEYID + ";Type:Android;" + "Version:"
                    + version + ";" + " clientType:newClient)";
            DB.PRE_URL = "http://" + DB.HTTPSERVER_HOST + ":"
                    + DB.HTTPSERVER_PORT + "/wisp_aas/adapter?open&url=";

            DB.HOMEPAGE_URL = BuildConfig.HOMEPAGE_URL;
            DB.LOGINPAGE_URL = BuildConfig.LOGINPAGE_URL;

            initDBFromSharedPreference(context);

            // 获取终端信息
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            DB.IMEI = telephonyManager.getDeviceId(); // 设备唯一标识号
            DB.MSISDN = telephonyManager.getLine1Number(); // 手机号码
            DB.IMSI = telephonyManager.getSubscriberId(); // SIM卡唯一标识号
            Log.e(TAG, "DB.IMEI:" + DB.IMEI);
            Log.e(TAG, "DB.IMSI:" + DB.IMSI);

            setUserAgent(context);
            Log.e(TAG, "DB.APP_VERSION_ID:" + DB.APP_VERSION_ID);
            DB.APP_VERSION_ID += "@@RJ@@" + DB.APP_CODE;
            Log.e(TAG, "DB.APP_VERSION_ID:" + DB.APP_VERSION_ID);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void initDBFromSharedPreference(Context context) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil
                .getSharedPreferences(context);
        // 从xml里面取一遍再从sharedPreferences里面取
        Boolean hasStoreFlag = sharedPreferences.getBoolean("hasStoreFlag",
                false);
        if (hasStoreFlag) {
            DB.APP_CODE = sharedPreferences.getString("APP_CODE", "");
            DB.APP_CHARSET = sharedPreferences.getString("APP_CHARSET", "");
            DB.APP_NAME = sharedPreferences.getString("APP_NAME", "");
            DB.SECURITY_HOST = sharedPreferences.getString("SECURITY_HOST",
                    "");
            DB.SECURITY_PORT = sharedPreferences.getInt("SECURITY_PORT", 0);
            DB.AAS_HOST = sharedPreferences.getString("AAS_HOST", "");
            DB.AAS_PORT = sharedPreferences.getInt("AAS_PORT", 5566);
            DB.LOGINPAGE_URL = sharedPreferences.getString("LOGINPAGE_URL",
                    "");
            DB.HOMEPAGE_URL = sharedPreferences.getString("HOMEPAGE_URL", "");
            DB.VPN_HOST = sharedPreferences.getString("vpnHost",
                    DB.VPN_HOST);
            DB.VPN_PORT = sharedPreferences.getInt("vpnPort", 443);
            DB.VPN_USER = sharedPreferences.getString("vpnUser", "sangfor");
            DB.VPN_PASS = sharedPreferences.getString("vpnPass", "admin");
            if (!TextUtils.isEmpty(DB.VPN_HOST)) {
                DB.USE_VPN = sharedPreferences.getBoolean("useVpn", true);
                Log.e(TAG, "DB.USE_VPN:" + DB.USE_VPN);
            }
        } else {
            saveConfig(context);
        }

    }

    public static void saveConfig(Context context) {
        try {
            /** Sharedpreferences方式读存储配置： */
            SharedPreferences.Editor editor = SharedPreferencesUtil.getSharedPreferences(
                    context).edit();
            Log.e(TAG, "host" + DB.SECURITY_HOST);
            Log.e(TAG, "app name" + DB.APP_NAME);
            editor.putString("APP_CODE", DB.APP_CODE);
            editor.putString("APP_CHARSET", DB.APP_CHARSET);
            editor.putString("APP_NAME", DB.APP_NAME);
            editor.putString("SECURITY_HOST", DB.SECURITY_HOST);
            editor.putInt("SECURITY_PORT", DB.SECURITY_PORT);
            editor.putString("AAS_HOST", DB.AAS_HOST);
            editor.putInt("AAS_PORT", DB.AAS_PORT);
            editor.putString("LOGINPAGE_URL", DB.LOGINPAGE_URL);
            editor.putString("HOMEPAGE_URL", DB.HOMEPAGE_URL);

            editor.putBoolean("hasStoreFlag", true);// 存储过这些变量，则从SharedPreferences取

            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void setUserAgent(Context context) {
        Log.e(TAG, "DB.KEYID:" + DB.KEYID);
        /**
         * strArr[0] zzgs001 应用标识 strArr[1] 03 客户端操作系统标识 01 iPhone；02 iPad；03
         * Android[平板]；04Android[手机];05Android[平板竖版] strArr[2] v1.0beat 应用版本标识
         */
        String version = AndroidTool.getVersionCode(context);
        if (DB.isPhone) {
            if (DB.isEpad) {
                DB.APP_VERSION_ID = "RJ-WISP-Client@@RJ@@03@@RJ@@V" + version;
                DB.USER_AGENT = " RJ-WISP-Client (IMEI:" + DB.IMEI + ";IMSI:"
                        + DB.IMSI + ";KEYID:" + DB.KEYID + ";Type:Android_Pad;"
                        + "Version:" + version + ";" + " clientType:newClient)";
            } else {
                // 由于aas把平板竖版代码单独弄了一套 所以目前就不用05平板竖版标识了
                // if (DB.isPortrait) {
                // DB.APP_VERSION_ID = "RJ-WISP-Client@@RJ@@05@@RJ@@V"
                // + version;
                // } else {
                DB.APP_VERSION_ID = "RJ-WISP-Client@@RJ@@04@@RJ@@V" + version;
                // }

                DB.USER_AGENT = " RJ-WISP-Client (IMEI:" + DB.IMEI + " ;IMSI:"
                        + DB.IMSI + "; KEYID:" + DB.KEYID + ";Type:Android;"
                        + "Version:" + version + ";" + " clientType:newClient)";
            }
        } else {
            DB.APP_VERSION_ID = "RJ-WISP-Client@@RJ@@03@@RJ@@V" + version;
            DB.USER_AGENT = " RJ-WISP-Client (IMEI:" + DB.IMEI + " ;IMSI:"
                    + DB.IMSI + " ;KEYID:" + DB.KEYID + ";Type:Android_Pad;"
                    + "Version:" + version + ";" + " clientType:newClient)";
        }
    }

    public static boolean initFilePath(Context context) {
        try {
//            DB.SDCARD_PATH = EnvironmentUtil.getExternalSdCardPath();
            DB.SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
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
