package com.rj.wisp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rj.framework.DB;
import com.rj.sdkey.view.PhoneLoginView;
import com.rj.view.ToastTool;
import com.rj.wisp.activity.LoginActivity;
import com.rj.wisp.base.BaseActivity;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.ui.phone.SettingActivity;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AppLoadActivity extends BaseActivity {
    private static final String TAG = AppLoadActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appload);

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
            checkSetting();
        }


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    getAppInfo();
////                    new LearnHttp().testHttp();
//                } catch (Exception e) {
//
//                }
//            }
//        }).start();
    }


    private void getAppInfo() throws Exception {
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        DataInputStream dis = null;
        try {
            // 1.socket请求
            socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(50000);
            os = socket.getOutputStream();
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os.write(("GET /wisp_aas/config/html/fgwlan/images/720/ico1.png HTTP/1.1"
                    + "\r\n").getBytes());
//            os.write(("GET /wisp_aas/adapter?open&_method=getAllAppInfo HTTP/1.1"
//                    + "\r\n").getBytes());
            os.write(("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n")
                    .getBytes());
            os.write(("User-Agent: newClient" + "\r\n").getBytes());
            os.write(("Accept-Language: zh-CN, en-US" + "\r\n").getBytes());
            os.write(("Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
                    + "\r\n").getBytes());
            os.write(("Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7"
                    + "\r\n").getBytes());
            os.write("\r\n".getBytes());
            os.flush();

//            // 2.socket响应
//            String s = "";
//            int contentLength = 0;
//            Map<String, String> map = SocketStreamUtil.readHeaders(is);
//            if (map.get("WISP-Content-Length") != null) {
//                try {
//                    contentLength = Integer.valueOf(map
//                            .get("WISP-Content-Length"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            byte[] buf = {};
//            int size = 0;
//            if (contentLength != 0) {
//                buf = new byte[contentLength];
//                while (size < contentLength) {
//                    int c = is.read();
//                    buf[size++] = (byte) c;
//
//                }
//                s = new String(buf, 0, size, "GBK");
//                Log.v("RJMOA", "消息体：" + s);
//            }
//
//            String jsonData = s;
//            Log.e(TAG, "jsonData:" + jsonData);
        } catch (Exception e) {
            e.printStackTrace();
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
            super.handleMessage(msg);
        }
    };

    @Override
    /**
     * 拦截MENU
     */
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
