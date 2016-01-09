package com.rj.wisp.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.anyoffice.sdk.SDKContext;
import com.huawei.anyoffice.sdk.login.LoginAgent;
import com.huawei.anyoffice.sdk.login.LoginParam;
import com.huawei.anyoffice.sdk.network.NetChangeCallback;
import com.huawei.anyoffice.sdk.network.NetStatusManager;
import com.rj.framework.DB;
import com.rj.view.ToastTool;
import com.rj.wisp.R;
import com.rj.wisp.base.BaseActivity;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.bean.ResourceMessageEvent;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.core.LocalSocketRequestTool;
import com.rj.wisp.ui.pad.PadMainActivity;
import com.rj.wisp.ui.phone.PhoneMainActivity;
import com.rj.wisp.ui.phone.PhoneSettingActivity;
import com.rj.wisp.widget.AppSettingDialog;

import java.io.File;
import java.net.InetSocketAddress;

import de.greenrobot.event.EventBus;

public class AppLoadActivity extends BaseActivity {
    private static final String TAG = AppLoadActivity.class.getName();
    private static final int CheckConnectionTimeAll = 6;
    private int checkConnectionTime = 0;
    private LocalSocketRequestTool localSocketRequestTool;

    @Override
    protected void onStop() {
        super.onStop();
//        EventBus.getDefault().post(new ResourceConfigSaveEvent());

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showLoginView() {
//        startService(new Intent(AppLoadActivity.this, NetConnectService.class));

//        WispCore.getWISPSO().CloseService();

        if (DB.isPhone) {
            Intent intent = new Intent(this, PhoneMainActivity.class);
            intent.putExtra("success", "true");
            intent.putExtra("popHomePageUrl", "");
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PadMainActivity.class);
            intent.putExtra("success", "true");
            intent.putExtra("popHomePageUrl", "");
            startActivity(intent);
        }

        finish();
    }


    // Called in Android UI's main thread
    public void onEventMainThread(ResourceMessageEvent event) {
        Log.e(TAG, "onEvent onEventMainThread:" + event.getEventType() + "--" + (event.getEventContent() != null ? event.getEventContent() : ""));

        if (event != null) {
            switch (event.getEventType()) {
                case ResourceMessageEvent.RESOURCE_DOWN_SUCC:
                case ResourceMessageEvent.RESOURCE_DOWN_FAIL:
                    updateDownResourceDialog();
                    break;
                case ResourceMessageEvent.RESOURCE_NO_UPDATE:
                    showMessage("无更新资源文件");
                    showLoginView();
                    break;
                case ResourceMessageEvent.RESOURCE_DOWN_END:
                    if (event.getEventContent() != null) {
                        int failResource = Integer.valueOf(event.getEventContent().toString());
                        if (failResource > 0) {
                            showMessage("下载失败" + failResource + "个资源文件");
                        } else {

                        }
                    }
                    if (downLoadDialog != null) {
                        downLoadDialog.dismiss();
                    }
                    showLoginView();
                    break;
                case ResourceMessageEvent.RESOURCE_DOWN_WRITE_FAIL_FAIL:
                    showMessage("资源配置文件保存失败,请联系管理员");
                    break;
                case ResourceMessageEvent.RESOURCE_GET_FAIL:
                case ResourceMessageEvent.RESOURCE_CONFIG_FORMAT_FAIL:
                    showMessage("获取资源列表出错,请重试");
                    break;
                case ResourceMessageEvent.RESOURCE_DOWN_START:
                    if (downLoadDialog != null) {
                        downLoadDialog.setMax(Integer.valueOf(event.getEventContent().toString()));
                        downLoadDialog.show();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private TextView messageTxt;

    @Override
    protected void onStart() {
        super.onStart();
        //注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        if (DB.isPhone && getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (!DB.isPhone) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        doLoginAnyOffice();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appload);
        messageTxt = (TextView) findViewById(R.id.messageTxt);



        localSocketRequestTool = new LocalSocketRequestTool();

        InitUtil.initDB(getApplicationContext());

        InitUtil.initFilePath(getApplicationContext());

        InitUtil.initSSLContext(getApplicationContext(), handler);

        InitUtil.initHttpServer(getApplicationContext(), handler);


//        if (DB.IS_PIN) {
//            PhoneLoginView login = new PhoneLoginView(getBaseContext(), new PhoneLoginView.IKeySdkService() {
//                @Override
//                public void loginSuccess() {
//                    ToastTool.show(AppLoadActivity.this, "验证成功", Toast.LENGTH_LONG);
//                    showLoginView();
//                }
//
//                @Override
//                public void exit() {
//                    finish();
//                }
//            });
//            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                    LinearLayout.LayoutParams.MATCH_PARENT);
//            addContentView(login, lParams);
//            // 初始化，识别SDKEY
//            login.initPin(AppLoadActivity.this);
//        } else {
//            checkSetting();
//        }

        initDownResourceDialog();


    }

    private ProgressDialog downLoadDialog;

    //多线程更新Dialog
    private void updateDownResourceDialog() {
        synchronized (this) {
            if (downLoadDialog != null) {
                downLoadDialog.setProgress(downLoadDialog.getProgress() + 1);
            }
        }
    }

    private void initDownResourceDialog() {
        if (downLoadDialog == null) {
            downLoadDialog = new ProgressDialog(this);
            downLoadDialog.setTitle("资源下载");
            downLoadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            downLoadDialog.setIndeterminate(false);
            downLoadDialog.setCancelable(false);
            downLoadDialog.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            resetButtonMethod();
                        }
                    });

            downLoadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            cancelButtonMethod();
                        }
                    });
        }
    }



    private void checkSetting() {
        if (TextUtils.isEmpty(DB.APP_CODE)) {
            ToastTool.show(AppLoadActivity.this, "没有获取到应用", Toast.LENGTH_SHORT);
            startActivity(new Intent(this, PhoneSettingActivity.class));
        } else {
            new MyAsyncTask().execute(CHECK_NETWORK);
        }
    }

    private void showMessage(String message) {
        try {
            if (!TextUtils.isEmpty(message)) {
                Log.e(TAG, "showMessage message:" + message);
                messageTxt.setText(message);
                ToastTool.show(AppLoadActivity.this, message, Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "msg.what:" + msg.what);
            switch (msg.what) {
                case HandlerWhat.UPDATE_MESSAGE:
                    String message = String.valueOf(msg.obj);
                    if (!TextUtils.isEmpty(message)) {
                        showMessage(message);
                    }
                    break;
                case HandlerWhat.NO_NEW_VERSION:
                    showMessage("无最新版本");
                    new MyAsyncTask().execute(CHECK_RESOURCE);
                    break;
                case HandlerWhat.HAS_NEW_VERSION:
                    break;
                case HandlerWhat.GET_NEW_VERSION_FAIL:
                    showMessage("获取版本更新失败");
                    new MyAsyncTask().execute(CHECK_RESOURCE);
                    break;
                case HandlerWhat.GET_CONNECTION_SUCC:
                    new MyAsyncTask().execute(CHECK_VERSION);
                    break;
                case HandlerWhat.GET_CONNECTION_FAIL:
                    if (checkConnectionTime++ > CheckConnectionTimeAll) {
                        showMessage("无法连接服务器,请检查网络配置是否正确!");
                    } else {
                        new MyAsyncTask().execute(CHECK_NETWORK);
                    }
                    break;
                case HandlerWhat.ANYOFFICE_LOGIN_SUCC:
                    try {
                        Log.e(TAG, "newState:" + msg.arg1 + " errorCode:"
                                + msg.arg2);
                        switch (msg.arg1) {
                            case NetStatusManager.NET_STATUS_ONLINE:
                                Log.e(TAG, "Tunnel is online");
                                showMessage("华为AnyOffice已上线:" + msg.arg1);
                                checkSetting();
                                break;
                            case NetStatusManager.NET_STATUS_OFFLINE:
                                showMessage("华为AnyOffice登录中:" + msg.arg1);
                                break;
                            default:
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case HandlerWhat.ANYOFFICE_LOGIN_FAIL:
                    showMessage("华为AnyOffice登录失败,请从平台启动本应用");
                    break;

            }
        }
    };


    private static final int CHECK_NETWORK = 1;
    private static final int CHECK_VERSION = 2;
    private static final int DOWN_NEW_VERSION = 3;
    private static final int CHECK_RESOURCE = 4;

    class MyAsyncTask extends AsyncTask<Object, Integer, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                Message msg = new Message();
                msg.what = HandlerWhat.UPDATE_MESSAGE;
                if (Integer.parseInt(params[0].toString()) == CHECK_NETWORK) {// 检测socket是否通
                    msg.obj = "连接服务器...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.checkConnection(handler);
                } else if (Integer.parseInt(params[0].toString()) == CHECK_VERSION) {// 获取更新信息
                    msg.obj = "获取更新信息...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.checkNewVersion(handler);
                } else if (Integer.parseInt(params[0].toString()) == DOWN_NEW_VERSION) {// 更新数据包
                    msg.obj = "更新数据包...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.updateVersion(params[1].toString());
                } else if (Integer.parseInt(params[0].toString()) == CHECK_RESOURCE) {// 更新资源
                    msg.obj = "更新资源...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.checkResources();
//                    AjaxGetResourcesTask ajaxGetResourcesTask = new AjaxGetResourcesTask();
//                    ajaxGetResourcesTask.execute();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // if (TextUtils.isEmpty(DB.SECURITY_HOST)) {
            // startActivity(new Intent(AppLoadActivity.this,
            // SettingActivity.class));
            // } else {
            // if (!TextUtils.isEmpty(DB.APP_URL)) {
            // startActivity(new Intent(AppLoadActivity.this,
            // LoginActivity.class));
            // }
            // }
            super.onPostExecute(result);
        }

    }

    private void showSetting() {
        if (DB.isPhone) {
            startActivity(new Intent(AppLoadActivity.this,
                    PhoneSettingActivity.class));
        } else {
            new AppSettingDialog(this).show();
        }
    }

    @Override
    protected void onTapMenuSelect(int position) {
        switch (position) {
            case 0:
                new MyAsyncTask().execute(CHECK_NETWORK);
                break;
            case 1:
//                init();
                new MyAsyncTask().execute(CHECK_NETWORK);
                break;
            case 2:
                showSetting();
                break;
            case 3:
                exitApp();
                break;
            default:
                break;
        }
    }

    /**
     * 拦截MENU
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (tabMenu != null) {
            if (tabMenu.isShowing())
                tabMenu.dismiss();
            else {
                tabMenu.showAtLocation(
                        findViewById(R.id.content),
                        Gravity.BOTTOM, 0, 0);
            }
        }
        return false;// 返回为true 则显示系统menu
    }

    /********************************************************************/

    private NetChangeCallback callback = new NetChangeCallback() {
        @Override
        public void onNetChanged(int oldState, int newState, int errorCode) {
            Log.e(TAG, String.format(
                    "oldState:%d, newState:%d, errorCode:%d, ", oldState,
                    newState, errorCode));
            Message msg = handler.obtainMessage();
            msg.what = HandlerWhat.ANYOFFICE_LOGIN_SUCC;
            msg.arg1 = newState;
            msg.arg2 = errorCode;
            handler.sendMessage(msg);
        }
    };

    private void doLoginAnyOffice() {
        showMessage("正在登录华为AnyOffice...");
        // 初始化SDK工作环境
//        boolean inited = SDKContext.getInstance().init(AppLoadActivity.this,
//                this.getCacheDir().getAbsolutePath());
        // 初始化SDK工作环境
        String workPath = "/data/data/" + getPackageName();
        File f = new File(workPath);
        if (!f.exists()) {
            f.mkdir();
        }
        Log.i(TAG, "Begin to init sdk envirenment");
        boolean inited = SDKContext.getInstance().init(AppLoadActivity.this,
                workPath);
        Log.i(TAG, "SDKContext.getInstance().init:" + inited);
        NetStatusManager.getInstance().setNetChangeCallback(callback);
        int status = NetStatusManager.getInstance().getNetStatus();
        Log.i(TAG, "NetStatusManager.getInstance().getNetStatus():" + status);
        LoginParam.UserInfo userInfo = LoginAgent.getInstance().getUserInfo();

        if (userInfo != null) {
            Log.i(TAG, "logined userInfo username:" + userInfo.userName
                    + ",password:" + userInfo.password);
        }else{
            Log.i(TAG, "logined userInfo null");
        }

        // login task
        AsyncTask<Object, Integer, Integer> loginTask = new AsyncTask<Object, Integer, Integer>() {
            @Override
            protected Integer doInBackground(Object... paramVarArgs) {
                Log.i(TAG, "login");

                String res = "Login SVN Success";
                // System.out.println("Begin to create login param");
                // 初始化登录参数
                LoginParam loginParam = new LoginParam();
                // 设置应用的业务类型，
                loginParam.setServiceType(getPackageName());
                loginParam.setLoginTitle("SvnSdkDemo");
                loginParam.setAutoLoginType(LoginParam.AutoLoginType.auto_login_enable);
                loginParam.setLoginBackground(true);


                loginParam.setUseSecureTransfer(true);
                System.out.println("Begin to login gateway");
                int ret = LoginAgent.getInstance().loginSync(
                        AppLoadActivity.this, loginParam);

                Log.i(TAG, "login result " + ret);
                if (ret == 0) {

                    int status = NetStatusManager.getInstance().getNetStatus();
                    Log.i(TAG, "NetStatusManager.getInstance().getNetStatus():"
                            + status);
                    if (status != NetStatusManager.NET_STATUS_CONNECTING) {
                        Message msg = handler.obtainMessage();
                        msg.what = HandlerWhat.ANYOFFICE_LOGIN_SUCC;
                        msg.arg1 = status;
                        msg.arg2 = ret;
                        handler.sendMessage(msg);
                    }
                } else {
                    res = "login error, ErrorCode:" + ret;
                    Message msg = handler.obtainMessage();
                    msg.what = HandlerWhat.ANYOFFICE_LOGIN_FAIL;
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
                            Log.i(TAG,
                                    "gatewayAddress:"
                                            + gatewayAddress.toString());
                        }

                        LoginParam.UserInfo userInfo = LoginAgent.getInstance()
                                .getUserInfo();

                        if (userInfo != null) {
                            Log.i(TAG, "logined userInfo username:"
                                    + userInfo.userName + ",password:"
                                    + userInfo.password);
                        } else {
                            Log.i(TAG, "no logined userInfo");
                        }
                    } else {
                        Log.i(TAG, "no loginParam");
                    }

                }
            }

        };
        // start login task
        loginTask.execute(new Object());

    }

}
