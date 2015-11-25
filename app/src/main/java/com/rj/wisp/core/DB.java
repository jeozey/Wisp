package com.rj.wisp.core;

import android.os.Environment;

import java.io.File;

/**
 * 作者：志文 on 2015/11/18 0018 15:16
 * 邮箱：594485991@qq.com
 */
public class DB {
    public static boolean isTestSSL = true;//测试ssl报错问题 暂时先加判断
    public static boolean isPhone;//phone or pad
    public static boolean isEpad;//是否E本
    public static boolean isPortrait = false;//是否竖屏显示,电建需要平板展示竖屏效果，其实就是手机的效果
    public static boolean isDianJian;//是否电建项目

    public static String HANDWRITE_PATH = Environment.getExternalStorageDirectory()
            + File.separator + "rjmoa" + File.separator + "handwriting";//手写图片路径

    /**
     * 安全中间件HOST/PORT
     */
    public static String SECURITY_HOST = "";
    public static int SECURITY_PORT = 0;

    //apn 名称
    public static String APN_NAME = "";
    public static String APP_NAME = "";
    public static String APP_VERSION_ID = "";
    public static String ERROR_URL = "";
    public static String USER_AGENT = "";
    public static String IMEI = "";
    public static String KEYID = "";
    public static String KEY_SERIAL = "";
    public static String MSISDN = "";
    public static String IMSI = "";
    public static String DataDirPath = "";
    public static String HOMEPAGE_URL = "";
    public static String APP_CHARSET = "";
    public static String AAS_HOST = "";
    public static Integer AAS_PORT = 5566;
    public static String APP_CODE = "";
    public static String APP_URL = "";//loginPage
    public static String HTTPSERVER_HOST = "127.0.0.1";
    public static Integer HTTPSERVER_PORT = 0;
    public static String PRE_URL = "";
    public static String CLIENT_KEY_STORE_PASSWORD = "";
    public static String CLIENT_TRUST_KEY_STORE_PASSWORD = "";

    public static boolean CHANGE_HOST = true;//是否可以修改应用地址和端口等
    public static boolean UPDATE_NEED_APPCODE = false;//获取版本更新信息时候是否发送appcode
    public static boolean IS_CONN_SAFESERVER = false;

    public static boolean USE_VPN = false;
    public static String VPN_HOST = "";
    public static int VPN_PORT = 443;
    public static String VPN_USER = "";
    public static String VPN_PASS = "";

    /****
     * 是否禁止设置界面输入
     ****/
    public static boolean IS_BANPUT = false;
    public static boolean IS_ULAN = false;
    public static boolean IS_PIN = false;
    public static boolean IS_SSLSOCKET = true;
    public static boolean IS_SDKEY = false;
    public static boolean IS_ORDINARY_SOCKET = false;


    /*************
     * 会议
     ****************/
    public static boolean NET_SWITCH = false;
    public static String NETWORK_NAME = "";
    public static String MEETING_IP = "220.250.1.46";
    public static String MEETING_PORT = "8888";
    public static String FILE_DISPERSION_SIZE = "10";
    public static String FILE_CACHE_SIZE = "5";
    public static boolean PDF_RESOLVE = false;
    public static boolean SCREEN_SHOT = false;
    /*************会议****************/


    public static String SDCARD_PATH = "";
    public static String CACHE_FILE_PATH = "";
    public static String EXTERNAL_FILE_PATH = "";
    public static String RESOURCE_PATH = "";
    public static String DOWN_FILE_PATH = "";

}
