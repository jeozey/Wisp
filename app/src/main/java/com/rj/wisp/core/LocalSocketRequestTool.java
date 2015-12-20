package com.rj.wisp.core;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.rj.framework.DB;
import com.rj.wisp.bean.HttpPkg;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;


public class LocalSocketRequestTool {
    private static final String TAG = LocalSocketRequestTool.class.getName();
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte[] CRLF = {CR, LF};
    private static final String _CRLF = "\r\n";
    private static final String CHECK_CONNECTION = "SOCKET /@@LocalSocket/checkConnection\r\n";
    private static final String VERSION_UPDATE = "SOCKET /@@LocalSocket/versionUpdate/android\r\n";
    private Handler handler = null;


    public HttpPkg getLocalSocketRequest(byte[] head, byte[] body) {
        try {
            Socket socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(60000);
            socket.setReceiveBufferSize(10240);
            OutputStream os = socket.getOutputStream();
            DataInputStream is = new DataInputStream(socket.getInputStream());
            os.write(head);
            os.write(CRLF);
            if (body != null) {
                os.write(body);
            }
            socket.shutdownOutput();
            HashMap<String, String> map = SocketStreamUtil.getHttpHead(is);
            String contentLength = map.get("Content-Length");
            HttpPkg httpPkg = new HttpPkg();
            httpPkg.setHead(map);
            Log.e(TAG, "head::" + map);
            if (!TextUtils.isEmpty(contentLength)) {
                int len = Integer.valueOf(contentLength);
                byte[] contentBody = SocketStreamUtil.getHttpBody(is, len);
                httpPkg.setBody(contentBody);
            }
            os.close();
            is.close();
            socket.close();
            return httpPkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Integer checkConnection(final Handler handler) {
        this.handler = handler;
        try {
            long start = System.currentTimeMillis();
            byte head[] = CHECK_CONNECTION.getBytes();
            HttpPkg httpPkg = getLocalSocketRequest(head, "".getBytes());

            String result = httpPkg.getHead().get("httpHead");
            long end = System.currentTimeMillis();
            long sub = end - start;

            Log.e(TAG, "result:" + result);
            Log.e(TAG, "" + sub + "毫秒");
            if (result != null && result.contains("true")) {
                Message msg = new Message();
                msg.obj = sub;
                String t = result.replace(" ", "").replace("\r", "")
                        .replace("\n", "");
                Log.e(TAG, "t:" + t);
                msg.getData().putString("result", t);
                msg.what = 4;
                handler.sendMessage(msg);

            } else {
                handler.sendEmptyMessage(-2);
            }
            Log.i(TAG, "关闭checkSocket方法中的socket");
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void checkNewVersion(final Handler handler) {
        this.handler = handler;
        try {

            byte head[] = VERSION_UPDATE.getBytes();
            HttpPkg httpPkg = getLocalSocketRequest(head, null);


            String result = new String(httpPkg.getBody());
            Log.v("BodyVersion:", "包体2:" + result);
//			Log.v("BodyVersion:", "result=" + result);
            if (result != null) {
                result = result.trim();
            }
            // 3.逻辑判断
            if ("THE_NEW_VERSION".equals(result)) {
                if (handler != null) {
                    handler.sendEmptyMessage(1);
                }
            } else {
                if (handler != null) {
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.getData().putString("result", result);
                    handler.sendMessage(msg);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "检查版本抛出异常了");
            e.printStackTrace();
            if (handler != null) {
                handler.sendEmptyMessage(-1);
            }
        }
    }


    public void updateVersion(String result) {
        // 1.socket下载请求
        try {

            String[] strArr = result.split("@@RJ@@");
            final String appInstallPackage = strArr[5]; // <app-install-package>

            Socket socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(10000);
            // socket.setKeepAlive(true);

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);

            StringBuffer reqStr = new StringBuffer();
            reqStr.append("POST /AjAxSocketIFC/downloadNewVersion\r\n");
            reqStr.append("Content-Length: " + appInstallPackage.length()
                    + "\r\n");
            reqStr.append("\r\n");
            reqStr.append(appInstallPackage);
            reqStr.append("\r\n");

            os.write(reqStr.toString().getBytes());
            os.flush();

            os.close();
            is.close();
            dis.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (handler != null) {
                handler.sendEmptyMessage(-1);
            }
        }
    }
}