package com.rj.wisp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rj.framework.DB;
import com.rj.sdkey.view.PhoneLoginView;
import com.rj.view.ToastTool;
import com.rj.wisp.base.BaseActivity;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.bean.ResourceConfigSaveEvent;
import com.rj.wisp.bean.ResourceMessageEvent;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.core.LocalSocketRequestTool;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.service.NetConnectService;
import com.rj.wisp.ui.pad.PadMainActivity;
import com.rj.wisp.ui.phone.PhoneMainActivity;
import com.rj.wisp.ui.phone.SettingActivity;

import de.greenrobot.event.EventBus;

public class AppLoadActivity extends BaseActivity {
    private static final String TAG = AppLoadActivity.class.getName();
    private static final int CheckConnectionTimeAll = 6;
    private int checkConnectionTime = 0;
    private LocalSocketRequestTool localSocketRequestTool;

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().post(new ResourceConfigSaveEvent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void showLoginView() {
//        startActivity(new Intent(this, PhoneMainActivity.class));

        WispCore.getWISPSO().CloseService();

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
                    ToastTool.show(getBaseContext(), "资源配置文件保存失败,请联系管理员", Toast.LENGTH_SHORT);
                    break;
                case ResourceMessageEvent.RESOURCE_GET_FAIL:
                case ResourceMessageEvent.RESOURCE_CONFIG_FORMAT_FAIL:
                    ToastTool.show(getBaseContext(), "获取资源列表出错,请重试", Toast.LENGTH_SHORT);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appload);
        messageTxt = (TextView) findViewById(R.id.messageTxt);



        localSocketRequestTool = new LocalSocketRequestTool();

        InitUtil.initDB(getApplicationContext());

        InitUtil.initFilePath(getApplicationContext());

        InitUtil.initSSLContext(getApplicationContext(), handler);

        InitUtil.initHttpServer(getApplicationContext(), handler);

        WispCore.getWISPSO().StartService(handler, getApplicationContext());


        if (DB.IS_PIN) {
            PhoneLoginView login = new PhoneLoginView(getBaseContext(), new PhoneLoginView.IKeySdkService() {
                @Override
                public void loginSuccess() {
                    ToastTool.show(AppLoadActivity.this, "验证成功", Toast.LENGTH_LONG);
                    showLoginView();
                }

                @Override
                public void exit() {
                    finish();
                }
            });
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            addContentView(login, lParams);
            // 初始化，识别SDKEY
            login.initPin(AppLoadActivity.this);
        } else {
            checkSetting();
        }

        initDownResourceDialog();


//        for(int i = 0;i<1000;i++) {
//
//            new MyTAsyncTask().execute();
//        }


//        AjaxGetResourcesTask ajaxGetResourcesTask = new AjaxGetResourcesTask();
//        ajaxGetResourcesTask.execute();

//        new MyAsyncTask().execute(CHECK_RESOURCE);
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

    class MyTAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            localSocketRequestTool.checkConnection(handler);
//            localSocketRequestTool.checkNewVersion(handler);
            return null;
        }
    }

    private void checkSetting() {
        if (TextUtils.isEmpty(DB.APP_CODE)) {
            ToastTool.show(getBaseContext(), "没有获取到应用", Toast.LENGTH_SHORT);
            startActivity(new Intent(this, SettingActivity.class));
        } else {
            new MyAsyncTask().execute(CHECK_NETWORK);
        }
    }

    private void showMessage(String message) {
        try {
            if (!TextUtils.isEmpty(message)) {
                Log.e(TAG, "showMessage message:" + message);
                messageTxt.setText(message);
                ToastTool.show(getBaseContext(), message, Toast.LENGTH_SHORT);
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
                    startService(new Intent(AppLoadActivity.this, NetConnectService.class));
                    new MyAsyncTask().execute(CHECK_VERSION);
                    break;
                case HandlerWhat.GET_CONNECTION_FAIL:
                    if (checkConnectionTime++ > CheckConnectionTimeAll) {
                        showMessage("无法连接服务器,请检查网络配置是否正确!");
                    } else {
                        new MyAsyncTask().execute(CHECK_NETWORK);
                    }
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
                    SettingActivity.class));
        } else {
//            new AppSettingDialog(this).show();
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

}
