package com.jeo.anyoffice;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.huawei.anyoffice.sdk.SDKContext;
import com.huawei.anyoffice.sdk.login.LoginAgent;
import com.huawei.anyoffice.sdk.login.LoginParam;
import com.huawei.anyoffice.sdk.network.NetChangeCallback;
import com.huawei.anyoffice.sdk.network.NetStatusManager;

import java.io.File;
import java.net.InetSocketAddress;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = new NetChangeCallback() {
            @Override
            public void onNetChanged(int oldState, int newState, int errorCode) {
                Log.e(TAG, String.format(
                        "oldState:%d, newState:%d, errorCode:%d, ", oldState,
                        newState, errorCode));
                Message msg = handler.obtainMessage();
                msg.what = 130;
                msg.arg1 = newState;
                msg.arg2 = errorCode;
                handler.sendMessage(msg);
            }
        };

        doLoginSvn();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            handleLoginResult(msg.arg1, msg.arg2);
        }
    };

    /**
     * handler all login status
     *
     * @param status
     */
    private void handleLoginResult(int status, int ret) {
        switch (status) {
            case NetStatusManager.NET_STATUS_ONLINE:
                Log.e(TAG, "Tunnel is online");
                Toast.makeText(getBaseContext(), "登陆成功", Toast.LENGTH_LONG);

                break;
            case NetStatusManager.NET_STATUS_OFFLINE:
                Toast.makeText(getBaseContext(), "登陆失败:" + ret, Toast.LENGTH_LONG);
                break;
            default:

                break;

        }
    }

    private NetChangeCallback callback;

    private void doLoginSvn() {
        // 初始化SDK工作环境
        String workPath = "/data/data/" + getPackageName();
        File f = new File(workPath);
        if (!f.exists()) {
            f.mkdir();
        }
        Log.e(TAG, "Begin to init sdk envirenment");
        boolean inited = SDKContext.getInstance().init(MainActivity.this,
                workPath);
        Log.e(TAG, "SDKContext.getInstance().init:" + inited);
        NetStatusManager.getInstance().setNetChangeCallback(callback);
        int status = NetStatusManager.getInstance().getNetStatus();
        Log.e(TAG, "NetStatusManager.getInstance().getNetStatus():" + status);
        LoginParam.UserInfo userInfo = LoginAgent.getInstance().getUserInfo();

        if (userInfo != null) {
            Log.e(TAG, "logined userInfo username:" + userInfo.userName
                    + ",password:" + userInfo.password);
        } else {
            Log.e(TAG, "logined userInfo null");
        }

        // login task
        AsyncTask<Object, Integer, Integer> loginTask = new AsyncTask<Object, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Object... paramVarArgs) {
                Log.e(TAG, "login");

                String res = "Login SVN Success";
                // System.out.println("Begin to create login param");
                // 初始化登录参数
                LoginParam loginParam = new LoginParam();
                // 设置应用的业务类型，
                loginParam.setServiceType(getPackageName());
                loginParam.setLoginTitle("SvnSdkDemo");
                loginParam.setAutoLoginType(LoginParam.AutoLoginType.auto_login_enable);
                loginParam.setLoginBackground(true);

                // 不设置登录参数，从AnyOffce获取，获取不到将界面输入

                // // 设置AnyOffice网关地址
                // loginParam.setInternetAddress(new InetSocketAddress(
                // DB.GATE_WAY, DB.GATE_WAY_PORT));
                // // // 设置用户信息
                // UserInfo userInfo = loginParam.new UserInfo();
                // userInfo.userName = "ceshi";
                // userInfo.password = "ceshi@123";
                // Log.e(TAG, "gateway:" + DB.GATE_WAY + ",port:"
                // + DB.GATE_WAY_PORT);
                // // Log.e(TAG, "LoginParam username:" + username +
                // ",password:"
                // // + password);
                // loginParam.setUserInfo(userInfo);

                loginParam.setUseSecureTransfer(true);
                System.out.println("Begin to login gateway");
                int ret = LoginAgent.getInstance().loginSync(
                        MainActivity.this, loginParam);

                Log.e(TAG, "login result " + ret);
                if (ret == 0) {

                    int status = NetStatusManager.getInstance().getNetStatus();
                    Log.e(TAG, "NetStatusManager.getInstance().getNetStatus():"
                            + status);
                    if (status != NetStatusManager.NET_STATUS_CONNECTING) {
                        Message msg = handler.obtainMessage();
                        msg.what = 130;
                        msg.arg1 = status;
                        msg.arg2 = ret;
                        handler.sendMessage(msg);
                    }
                } else {
                    res = "login error, ErrorCode:" + ret;
                    Message msg = handler.obtainMessage();
                    msg.what = 131;
                    handler.sendMessage(msg);
                }

                return ret;
            }

            protected void onPostExecute(Integer result) {
                if (result != 0) {
                    // showOfflineView();
                } else {
                    LoginParam loginParam = LoginAgent.getInstance()
                            .getLoginParam();
                    if (loginParam != null) {
                        InetSocketAddress gatewayAddress = loginParam
                                .getInternetAddress();
                        if (gatewayAddress != null) {
                            Log.e(TAG,
                                    "gatewayAddress:"
                                            + gatewayAddress.toString());
                        }

                        LoginParam.UserInfo userInfo = LoginAgent.getInstance()
                                .getUserInfo();

                        if (userInfo != null) {
                            Log.e(TAG, "logined userInfo username:"
                                    + userInfo.userName + ",password:"
                                    + userInfo.password);
                        } else {
                            Log.e(TAG, "no logined userInfo");
                        }
                    } else {
                        Log.e(TAG, "no loginParam");
                    }

                }
            }

        };
        // start login task
        loginTask.execute(new Object());

    }
}
