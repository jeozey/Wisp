package com.rj.wisp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rj.framework.DB;
import com.rj.sdkey.view.PhoneLoginView;
import com.rj.view.ToastTool;
import com.rj.wisp.activity.LoginActivity;
import com.rj.wisp.base.BaseActivity;
import com.rj.wisp.bean.MessageEvent;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.core.LocalSocketRequestTool;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.task.AjaxGetResourcesTask;
import com.rj.wisp.ui.phone.SettingActivity;

import de.greenrobot.event.EventBus;

public class AppLoadActivity extends BaseActivity {
    private static final String TAG = AppLoadActivity.class.getName();

    private LocalSocketRequestTool localSocketRequestTool;

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(MessageEvent event) {
        Log.e(TAG, "onEvent MessageEvent:" + (event.getEventContent() != null ? event.getEventContent() : ""));
        if (event != null) {
            switch (event.getEventType()) {
                case MessageEvent.RESOURCE_DOWN_START:
                    if (downLoadDialog != null) {
                        Looper.prepare();
                        downLoadDialog.setMax(Integer.valueOf(event.getEventContent().toString()));
                        downLoadDialog.show();
                    }
                    break;
                case MessageEvent.RESOURCE_DOWN_SUCC:
                case MessageEvent.RESOURCE_DOWN_FAIL:
                    if (downLoadDialog != null) {
                        updateDownResourceDialog();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appload);

        //注册EventBus
        EventBus.getDefault().register(this);

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
                    startActivity(new Intent(AppLoadActivity.this, LoginActivity.class));
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
//            checkSetting();
        }

//for(int i = 0;i<1000;i++) {
//    new Thread(new Runnable() {
//        @Override
//        public void run() {
////                localSocketRequestTool.checkConnection(handler);
//            StringBuilder sb = new StringBuilder();
//            sb.append("GET /wisp_aas/config/html/law/css/bootstrap.min.css HTTP/1.1" + "\r\n");
////            sb.append("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n");
////            sb.append("Method-Type: download" + "\r\n");
//////        sb.append("File-Time: " + modified + "\r\n");
//////        sb.append("File-Type: " + filetype + "\r\n");
//////        sb.append("File-Name: " + filepath + "\r\n");
////            sb.append("Accept: */*" + "\r\n");
////            sb.append("Accept-Encoding: gzip, deflate" + "\r\n");
////            sb.append("Accept-Language: zh-CN, en-US" + "\r\n");
////            sb.append("Connection: Keep-Alive" + "\r\n");
//            new LocalSocketRequestTool().getLocalSocketRequest(sb.toString().getBytes(), null);
//
//        }
//    }).start();
//    try {
//        Thread.sleep(1000);
//    } catch (InterruptedException e) {
//        e.printStackTrace();
//    }
//}

//        for(int i = 0;i<1000;i++) {
//
//            new MyTAsyncTask().execute();
//        }

        initDownResourceDialog();
        downLoadDialog.show();
        AjaxGetResourcesTask ajaxGetResourcesTask = new AjaxGetResourcesTask(
                AppLoadActivity.this, handler);
        ajaxGetResourcesTask.execute();
    }

    private ProgressDialog downLoadDialog;

    //多线程更新Dialog
    private void updateDownResourceDialog() {
        synchronized (this) {
            downLoadDialog.setProgress(downLoadDialog.getProgress() + 1);
        }
    }

    private void initDownResourceDialog() {
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
            ToastTool.show(getBaseContext(), "初始化成功", Toast.LENGTH_SHORT);
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "msg.what:" + msg.what);
            switch (msg.what) {

            }
        }
    };


    class MyAsyncTask extends AsyncTask<Object, Integer, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                Message msg = new Message();
                msg.what = 5;
                if (Integer.parseInt(params[0].toString()) == 5) {// 检测socket是否通
                    msg.obj = "连接服务器...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.checkConnection(handler);
                } else if (Integer.parseInt(params[0].toString()) == 1) {// 获取更新信息
                    msg.obj = "获取更新信息...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.checkNewVersion(handler);
                } else if (Integer.parseInt(params[0].toString()) == 2) {// 更新数据包
                    msg.obj = "更新数据包...";
                    handler.sendMessage(msg);
                    localSocketRequestTool.updateVersion(params[1].toString());
                } else if (Integer.parseInt(params[0].toString()) == 3) {// 更新资源
                    msg.obj = "更新资源...";
                    handler.sendMessage(msg);
//                    AjaxGetResourcesTask ajaxGetResourcesTask = new AjaxGetResourcesTask(
//                            AppLoadActivity.this, handler);
//                    ajaxGetResourcesTask.execute();
                }
                handler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(-1);
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
            case 1:
//                init();
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
