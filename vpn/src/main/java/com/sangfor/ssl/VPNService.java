package com.sangfor.ssl;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sangfor.ssl.common.VpnCommon;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class VPNService extends Service implements IVpnDelegate {
    private static final String TAG = VPNService.class.getName();
    /**
     * L3VPN重新登录
     **/
    public static final int RESULT_VPN_L3VPN_RELOGIN = -5;

    public static final int RESULT_VPN_L3VPN_FATAL = -4;
    /**
     * L3VPN服务启动失败
     **/
    public static final int RESULT_VPN_L3VPN_FAIL = -3;
    /**
     * 初始化vpn失败
     **/
    public static final int RESULT_VPN_INIT_FAIL = -2;
    /**
     * 认证失败
     **/
    public static final int RESULT_VPN_AUTH_FAIL = -1;
    /**
     * 无效值
     **/
    public static final int RESULT_VPN_NONE = 0;
    /**
     * 初始化vpn成功
     **/
    public static final int RESULT_VPN_INIT_SUCCESS = 1;
    /**
     * 认证成功
     **/
    public static final int RESULT_VPN_AUTH_SUCCESS = 2;
    /**
     * vpn注销了
     **/
    public static final int VPN注销 = 3;
    /**
     * 认证取消了
     **/
    public static final int RESULT_VPN_AUTH_CANCEL = 4;
    /**
     * L3vpn启动成功
     **/
    public static final int RESULT_VPN_L3VPN_SUCCESS = 5;
    /**
     * 其他返回状态
     **/
    public static final int RESULT_VPN_OTHER = 6;

//	 private static final String VPN_HOST = "219.159.82.135";
//	 private static final int VPN_PORT = 443;
//	 private static final String VPN_USER = "mobileuser";
//	 private static final String VPN_PASS = "123456";

//	private static final String VPN_HOST = "220.191.253.188";
//	private static final int VPN_PORT = 443;
//	private static final String VPN_USER = "RJtest";
//	private static final String VPN_PASS = "123456";

    private static boolean isInit = false;
    private static boolean isLogin = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            try {
                Log.e(TAG, "msg.what:" + msg.what);
                Intent i = new Intent("com.rj.vpnservice");
                i.putExtra("msg", msg.what);
                getBaseContext().sendBroadcast(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public VPNService() {
    }

    private String VPN_HOST;
    private int VPN_PORT;
    private String VPN_USER;
    private String VPN_PASS;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.e(TAG, "onStartCommand");
            if (intent != null && "logout".equals(intent.getStringExtra("type"))) {
                Log.e("type", "logout isInit:" + isInit + " isLogin:" + isLogin);
                SangforAuth.getInstance().vpnLogout();
//				SangforAuth.getInstance().vpnQuit();
            } else if (intent != null && "login".equals(intent.getStringExtra("type"))) {
                Log.e("type", "login isInit:" + isInit + " isLogin:" + isLogin);
                VPN_HOST = intent.getStringExtra("VPN_HOST");
                VPN_PORT = intent.getIntExtra("VPN_PORT", 443);
                VPN_USER = intent.getStringExtra("VPN_USER");
                VPN_PASS = intent.getStringExtra("VPN_PASS");
                Log.e(TAG, "VPN_HOST:" + VPN_HOST + " VPN_PORT:" + VPN_PORT + " VPN_USER:" + VPN_USER + " VPN_PASS:" + VPN_PASS);
                if (!isInit) {
                    initVpn();
                }
                if (!isLogin) {
                    initSslVpn();
                }
            } else {
                Log.e(TAG, "stopSelf");
                stopSelf();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void initVpn() {
        try {
            com.sangfor.ssl.service.utils.logger.Log.init(getApplicationContext());
            com.sangfor.ssl.service.utils.logger.Log.LEVEL = com.sangfor.ssl.service.utils.logger.Log.ERROR;
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        SangforAuth sfAuth = SangforAuth.getInstance();
        try {
            sfAuth.init(getBaseContext(), this, SangforAuth.AUTH_MODULE_EASYAPP);
            sfAuth.setLoginParam(AUTH_CONNECT_TIME_OUT, String.valueOf(5));
        } catch (SFException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reloginCallback(int status, int result) {
        switch (status) {

            case IVpnDelegate.VPN_START_RELOGIN:
                Log.e(TAG, "relogin callback start relogin start ...");
                break;
            case IVpnDelegate.VPN_END_RELOGIN:
                Log.e(TAG, "relogin callback end relogin ...");
                if (result == IVpnDelegate.VPN_RELOGIN_SUCCESS) {
                    Log.e(TAG, "relogin callback, relogin success!");
                } else {
                    Log.e(TAG, "relogin callback, relogin failed");
                }
                break;
        }

    }

    @Override
    public void vpnRndCodeCallback(byte[] arg0) {

    }

    InetAddress m_iAddr;

    /**
     * 开始初始化VPN，该初始化为异步接口，后续动作通过回调函数通知结果
     *
     * @return 成功返回true，失败返回false，一般情况下返回true
     */
    private boolean initSslVpn() {
        SangforAuth sfAuth = SangforAuth.getInstance();
        Log.i(TAG, "================ initSslVpn ===");
        m_iAddr = null;
        final String ip = VPN_HOST;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    m_iAddr = InetAddress.getByName(ip);
                    Log.i(TAG, "ip Addr is : " + m_iAddr.getHostAddress());
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (m_iAddr == null || m_iAddr.getHostAddress() == null) {
            Log.d(TAG, "vpn host error");
            return false;
        }
        long host = VpnCommon.ipToLong(m_iAddr.getHostAddress());
        int port = VPN_PORT;

        if (sfAuth.vpnInit(host, port) == false) {
            Log.d(TAG, "vpn init fail, errno is " + sfAuth.vpnGeterr());
            return false;
        }

        return true;
    }

    /**
     * 处理认证，通过传入认证类型（需要的话可以改变该接口传入一个hashmap的参数用户传入认证参数）.
     * 也可以一次性把认证参数设入，这样就如果认证参数全满足的话就可以一次性认证通过，可见下面屏蔽代码
     *
     * @param authType 认证类型
     * @throws SFException
     */
    private void doVpnLogin(int authType) {
        Log.d(TAG, "doVpnLogin authType " + authType);

        boolean ret = false;
        SangforAuth sfAuth = SangforAuth.getInstance();

        switch (authType) {
            case IVpnDelegate.AUTH_TYPE_PASSWORD:
                String user = VPN_USER;
                String passwd = VPN_PASS;
                sfAuth.setLoginParam(IVpnDelegate.PASSWORD_AUTH_USERNAME, user);
                sfAuth.setLoginParam(IVpnDelegate.PASSWORD_AUTH_PASSWORD, passwd);
                ret = sfAuth.vpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
                break;
            default:
                Log.w(TAG, "default authType " + authType);
                break;
        }

        if (ret == true) {
            Log.i(TAG, "success to call login method");
        } else {
            Log.i(TAG, "fail to call login method");
        }

    }


    @Override
    public void vpnCallback(int vpnResult, int authType) {
        SangforAuth sfAuth = SangforAuth.getInstance();

        switch (vpnResult) {
            case IVpnDelegate.RESULT_VPN_INIT_FAIL:
                /**
                 * 初始化vpn失败
                 */
                Log.i(TAG, "VPN初始化失败: " + sfAuth.vpnGeterr());
                displayToast("VPN初始化失败: " + sfAuth.vpnGeterr());
                break;

            case IVpnDelegate.RESULT_VPN_INIT_SUCCESS:
                /**
                 * 初始化vpn成功，接下来就需要开始认证工作了
                 */
                Log.i(TAG,
                        "VPN初始化完成: " + sfAuth.vpnQueryStatus());
                displayToast("VPN初始化完成: "
                        + sfAuth.vpnQueryStatus());
                Log.i(TAG, "vpnResult===================" + vpnResult + "\nauthType ==================" + authType);
                // 初始化成功，进行认证操作
                doVpnLogin(IVpnDelegate.AUTH_TYPE_PASSWORD);
//				doVpnLogin(IVpnDelegate.AUTH_TYPE_CERTIFICATE);
//				doVpnLogin(authType);
//				String hardidString = sfAuth.vpnQueryHardID();
//				Log.w(TAG, "vpn hardid ============================ " + hardidString);
                break;

            case IVpnDelegate.RESULT_VPN_AUTH_FAIL:
                /**
                 * 认证失败，有可能是传入参数有误，具体信息可通过sfAuth.vpnGeterr()获取
                 */
                String errString = sfAuth.vpnGeterr();
                Log.i(TAG, "VPN认证失败: " + sfAuth.vpnGeterr());
                displayToast("VPN认证失败: " + sfAuth.vpnGeterr());
                break;

            case IVpnDelegate.RESULT_VPN_AUTH_SUCCESS:
                /**
                 * 认证成功，认证成功有两种情况，一种是认证通过，可以使用sslvpn功能了，另一种是前一个认证（如：用户名密码认证）通过，
                 * 但需要继续认证（如：需要继续证书认证）
                 */
                if (authType == IVpnDelegate.AUTH_TYPE_NONE) {
                    Log.i(TAG, "VPN登录成功!");
                    displayToast("VPN登录成功!");
                    handler.sendEmptyMessage(12);
                } else {
                    Log.i(TAG, "auth success, and need next auth, next auth type is " + authType);
                    displayToast("auth success, and need next auth, next auth type is " + authType);

                    if (authType == IVpnDelegate.AUTH_TYPE_SMS) {
                        // 输入短信验证码
                        Toast.makeText(this, "you need send sms code.", Toast.LENGTH_LONG).show();
                    } else {
                        doVpnLogin(authType);
                    }
                }
                break;
            case IVpnDelegate.RESULT_VPN_AUTH_CANCEL:
                Log.i(TAG, "RESULT_VPN_AUTH_CANCEL");
                displayToast("RESULT_VPN_AUTH_CANCEL");
                break;
            case IVpnDelegate.RESULT_VPN_AUTH_LOGOUT:
                /**
                 * 主动注销（自己主动调用logout接口）或者被动注销（通过控制台把用户踢掉）均会调用该接口
                 */
                Log.i(TAG, "VPN注销");
                displayToast("VPN注销");
                stopSelf();
                break;
            case IVpnDelegate.VPN_STATUS_ONLINE:
                /**
                 * 与设备连接建立
                 */
                Log.i(TAG, "online");
                displayToast("online");
                break;
            case IVpnDelegate.VPN_STATUS_OFFLINE:
                /**
                 * 与设备连接断开
                 */
                Log.i(TAG, "offline");
                displayToast("offline");
                break;
            default:
                /**
                 * 其它情况，不会发生，如果到该分支说明代码逻辑有误
                 */
                Log.i(TAG, "default result, vpn result is " + vpnResult);
                displayToast("default result, vpn result is " + vpnResult);
                break;
        }

    }

    private void displayToast(String msg) {
        Log.e(TAG, "msg:" + msg);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
