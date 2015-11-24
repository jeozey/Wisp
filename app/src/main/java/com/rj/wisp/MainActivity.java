package com.rj.wisp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rj.connection.ISocketConnection;
import com.rj.connection.SocketConnectionManager;
import com.rj.connection.SocketConnectionPool;
import com.rj.sdkey.view.PhoneLoginView;
import com.rj.util.AndroidTool;
import com.rj.util.SocketStreamUtil;
import com.rj.util.ToastTool;
import com.rj.view.msg.AppMsg;
import com.rj.wisp.activity.LoginActivity;
import com.rj.wisp.core.DB;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.core.WispCore;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import static com.rj.view.msg.AppMsg.LENGTH_LONG;
import static com.rj.view.msg.AppMsg.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();

    @Override
    public void onClick(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkConnection(handler);
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        findViewById(R.id.title).setOnClickListener(this);
        DB.SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
        DB.isPhone = true;
        DB.HTTPSERVER_HOST = "127.0.0.1";
        DB.HTTPSERVER_PORT = 8011;
        DB.SECURITY_HOST = "220.250.1.46";
        DB.SECURITY_PORT = 6611;
        DB.APP_CODE = "NcysATJnLGRkNGBuO9VkNGRwLWYZ49E9NGZmNXJoNWVrOGZpOGNnRpRmN9ZjNGRjNpUvQd3bNHem59Vq3Cbg";
        DB.IS_PIN = true;


        String version = AndroidTool.getVersionCode(getBaseContext());
        DB.USER_AGENT = " RJ-WISP-Client (IMEI:" + DB.IMEI + " ;IMSI:" + DB.IMSI + "; KEYID:" + DB.KEYID + ";Type:Android;" + "Version:"
                + version + ";" + " clientType:newClient)";
        com.rj.framework.DB.USER_AGENT = DB.USER_AGENT;
        DB.PRE_URL = "http://" + DB.HTTPSERVER_HOST + ":"
                + DB.HTTPSERVER_PORT + "/wisp_aas/adapter?open&url=";

        DB.HOMEPAGE_URL = "http://192.168.1.12/homepage.nsf/homepage?OpenForm";
        DB.APP_URL = "http://192.168.1.12/homepage.nsf";
//        DB.APP_URL = "http://127.0.0.1:8011/wisp_aas/ClientInfo.jsp?url=http%3A%2F%2F192.168.1.12%2Fhomepage.nsf";

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
        AppMsg.Style style = AppMsg.STYLE_INFO;
        int time = Toast.LENGTH_SHORT;
        if (Toast.LENGTH_LONG == time) {
            style = new AppMsg.Style(LENGTH_LONG, com.rj.util.R.color.custom);
        } else {
            style = new AppMsg.Style(LENGTH_SHORT, com.rj.util.R.color.custom);
        }


        if (DB.IS_PIN) {
            PhoneLoginView login = new PhoneLoginView(getBaseContext(), new PhoneLoginView.IKeySdkService() {
                @Override
                public void loginSuccess() {
                    ToastTool.show(MainActivity.this, "验证成功", Toast.LENGTH_LONG);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
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
            login.initPin(MainActivity.this);
        } else {

            startActivity(new Intent(this, LoginActivity.class));
        }


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    getAppInfo();
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
        GZIPInputStream gzipInputStream = null;
        DataInputStream dis = null;
        try {
            // 1.socket请求
            socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(50000);
            os = socket.getOutputStream();
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os.write(("GET /wisp_aas/adapter?open&_method=getAllAppInfo HTTP/1.1"
                    + "\r\n").getBytes());
            os.write(("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n")
                    .getBytes());
            os.write(("Accept-Encoding: gzip" + "\r\n").getBytes());
            os.write(("User-Agent: newClient" + "\r\n").getBytes());
            os.write(("Accept-Language: zh-CN, en-US" + "\r\n").getBytes());
            os.write(("Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
                    + "\r\n").getBytes());
            os.write(("Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7"
                    + "\r\n").getBytes());
            os.write("\r\n".getBytes());
            os.flush();

            // 2.socket响应
            String s = "";
            int contentLength = 0;
            Log.e("wufeng", "app get gzip");
            HashMap<String, String> map = SocketStreamUtil.getHttpHead2(is);
            if (map.get("WISP-Content-Length") != null) {
                try {
                    contentLength = Integer.valueOf(map
                            .get("WISP-Content-Length"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!DB.isTestSSL) {
                gzipInputStream = new GZIPInputStream(is);
            }

            byte[] buf = {};
            int size = 0;
            if (contentLength != 0) {
                buf = new byte[contentLength];
                while (size < contentLength) {
                    if (DB.isTestSSL) {
                        int c = is.read();
                        buf[size++] = (byte) c;
                    } else {
                        int c = gzipInputStream.read();
                        buf[size++] = (byte) c;
                    }

                }
                s = new String(buf, 0, size, "GBK");
                Log.v("RJMOA", "消息体：" + s);
            }

            String jsonData = s;
            Log.e(TAG, "jsonData:" + jsonData);
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

}
