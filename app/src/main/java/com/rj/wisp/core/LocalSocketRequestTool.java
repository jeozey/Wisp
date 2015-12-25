package com.rj.wisp.core;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rj.framework.DB;
import com.rj.wisp.bean.ConnectionStatus;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.bean.HttpPkg;
import com.rj.wisp.task.AjaxGetResourcesTask;

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
    private static final int TIME_OUT = 60000;
    private static final int CONNECTION_TIME_OUT = 20000;//检测网络连接,时间较短
    private static final String CHECK_CONNECTION = "SOCKET /@@LocalSocket/checkConnection\r\n";
    private static final String VERSION_UPDATE = "SOCKET /@@LocalSocket/versionUpdate/android\r\n";
    private static final String THE_NEW_VERSION = "THE_NEW_VERSION";


    public HttpPkg getLocalSocketRequest(byte[] head, byte[] body) {
        return getLocalSocketRequest(head, body, TIME_OUT);
    }

    public HttpPkg getLocalSocketRequest(byte[] head, byte[] body, int timeOut) {
        try {
            Socket socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(timeOut);
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
//            Log.e(TAG, "head::" + map);
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


    public void checkConnection(final Handler handler) {
        try {
            long start = System.currentTimeMillis();
            byte head[] = CHECK_CONNECTION.getBytes();
            HttpPkg httpPkg = getLocalSocketRequest(head, "".getBytes(), CONNECTION_TIME_OUT);

            String result = null;
            if (httpPkg.getBody() == null) {
                result = httpPkg.getHead().get("httpHead");
            } else {
                result = new String(httpPkg.getBody());
            }
            long end = System.currentTimeMillis();
            long sub = end - start;

            Log.e(TAG, "result:" + result);
            Log.e(TAG, "" + sub + "毫秒");
            if (result != null && result.contains("true")) {
                Message msg = handler.obtainMessage(HandlerWhat.GET_CONNECTION_SUCC);
                ConnectionStatus status = new ConnectionStatus();
                status.setToServerTime(sub);
                JSONObject obj = JSON.parseObject(result);
                long time = obj.getLong("msg");
                status.setToOaTime(time);
                Log.e(TAG, "status0:" + status);
                msg.obj = status;
                handler.sendMessage(msg);
            } else {
                handler.sendEmptyMessage(HandlerWhat.GET_CONNECTION_FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(HandlerWhat.GET_CONNECTION_FAIL);
        }
    }

    public void checkResources() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("GET /wisp_aas/adapter?open&_method=getResourcesList&appcode="
                    + DB.APP_CODE + " HTTP/1.1" + "\r\n");
            sb.append("User-agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1;"
                    + DB.USER_AGENT + ")\r\n");
            sb.append("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n");
            sb.append("Accept-Language: zh-CN, en-US" + "\r\n");
            sb.append(
                    "Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
                            + "\r\n");
            sb.append("Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7"
                    + "\r\n");

            HttpPkg httpPkg = getLocalSocketRequest(sb.toString().getBytes(), null);

            byte[] content = httpPkg.getBody();
            String charset = httpPkg.getHead().get("charset");
            String jsonData = new String(content, charset != null ? charset : "GBK");

            AjaxGetResourcesTask ajaxGetResourcesTask = new AjaxGetResourcesTask();
            ajaxGetResourcesTask.execute(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkNewVersion(final Handler handler) {
        try {

            byte head[] = VERSION_UPDATE.getBytes();
            HttpPkg httpPkg = getLocalSocketRequest(head, null);


            String result = new String(httpPkg.getBody());
            Log.v("BodyVersion:", "包体2:" + result);
            if (result != null) {
                result = result.trim();
            }
            // 3.逻辑判断
            if (THE_NEW_VERSION.equals(result)) {
                if (handler != null) {
                    handler.sendEmptyMessage(HandlerWhat.NO_NEW_VERSION);
                }
            } else {
                if (handler != null) {
                    Message msg = Message.obtain(handler, HandlerWhat.HAS_NEW_VERSION);
                    msg.obj = result;
                    handler.sendMessage(msg);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (handler != null) {
                handler.sendEmptyMessage(HandlerWhat.GET_NEW_VERSION_FAIL);
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
        }
    }
}