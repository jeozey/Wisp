package com.rj.wisp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rj.connection.ISocketConnection;
import com.rj.connection.SocketConnectionManager;
import com.rj.connection.SocketConnectionPool;
import com.rj.framework.DB;
import com.rj.sdkey.view.PhoneLoginView;
import com.rj.view.ToastTool;
import com.rj.wisp.activity.LoginActivity;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.ui.phone.SettingActivity;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;

public class AppLoadActivity extends AppCompatActivity {
    private static final String TAG = AppLoadActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InitUtil.initDB(getApplicationContext());

        InitUtil.initFilePath(getApplicationContext());

        InitUtil.initSSLContext(getApplicationContext(), handler);

        InitUtil.initHttpServer(getApplicationContext(), handler);

        WispCore.getWISPSO().StartService(handler, getApplicationContext());


//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                        checkConnection(handler);
//            }
//        },1000,5000);

//        checkConnection();


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

            startActivity(new Intent(this, SettingActivity.class));
//            startActivity(new Intent(this, LoginActivity.class));
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


    public Integer checkConnection(final Handler handler) {
        int back = 0;
        this.handler = handler;
        try {
            long start = System.currentTimeMillis();
            Log.e(TAG, "DB.HTTPSERVER_HOST:" + DB.HTTPSERVER_HOST);
            Log.e(TAG, "DB.HTTPSERVER_PORT:" + DB.HTTPSERVER_PORT);
            // 1.socket请求

            /** *2.与中间件交互** */
            SocketConnectionPool connectionPool = SocketConnectionManager
                    .getInstance().getSocketConnectionPool();
            ISocketConnection socket = connectionPool.getConnection(
                    DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT, SocketConnectionPool.SOCKET_TYPE.ORIDINARY_SOCKET);

//            Socket socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
//            Log.e(TAG,"socket:"+socket);
//            socket.setSoTimeout(10000);// 10秒超时来判断是否能连得上服务器
//            // socket.setKeepAlive(true);
//            socket.setReceiveBufferSize(10240);

            OutputStream os = socket.getOutputStream();

            os.write(("SOCKET /AjAxSocketIFC/checkConnection" + "\r\n")
                    .getBytes());
            os.flush();
//            InputStream is = socket.getInputStream();
//
//            HashMap<String, String> map = SocketStreamUtil.getHttpResponse(is);
//            if (map != null) {
//                Log.e(TAG, "map!=null," + map);
//            } else {
//                handler.sendEmptyMessage(-2);
//                Log.e(TAG, "map为空");
//                os.close();
//                is.close();
//                socket.close();
//                Log.i(TAG, "关闭checkSocket方法中的socket");
//                back = -2;
//                return back;
//            }
            HashMap<String, String> map = socket.getHttpHead2();
            if (map != null) {
                Log.e(TAG, "map!=null," + map);
            } else {
                handler.sendEmptyMessage(-2);
                Log.e(TAG, "map为空");
                os.close();
//                is.close();
                socket.close();
                Log.i(TAG, "关闭checkSocket方法中的socket");
                back = -2;
                return back;
            }

            String result = map.get("httpHead");
            long end = System.currentTimeMillis();
            long sub = end - start;

            Log.e(TAG, "result:" + result);
            if (map != null && map.get("httpHead") != null
                    && map.get("httpHead").contains("true")) {
                Message msg = new Message();
                msg.obj = sub;
                String t = result.replace(" ", "").replace("\r", "")
                        .replace("\n", "");
                Log.e(TAG, "t:" + t);
                msg.getData().putString("result", t);
                msg.what = 4;
                handler.sendMessage(msg);
                back = 4;
            } else {
                handler.sendEmptyMessage(-2);
                back = -2;
            }
            os.close();
//            is.close();
            socket.close();
            Log.i(TAG, "关闭checkSocket方法中的socket");
        } catch (ConnectException e0) {
            e0.printStackTrace();
            //本地服务8011未开启 说明应用未启动或已退出
            back = -1;
        } catch (Exception e) {
            e.printStackTrace();
            if (handler != null) {
                handler.sendEmptyMessage(-2);
                back = -2;
            }
        }
        return back;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

}
