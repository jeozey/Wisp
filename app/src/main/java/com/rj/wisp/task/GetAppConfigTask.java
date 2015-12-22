package com.rj.wisp.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.rj.framework.DB;
import com.rj.wisp.core.SocketStreamUtil;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;


/***
 * ajax 获取应用配置
 */
public class GetAppConfigTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = GetAppConfigTask.class.getName();
    private String jsonData = null;
    private ProgressDialog appLoadDialog = null;
    private Context context;
    private GetAppConfigCallBack getAppConfigCallBack;

    public interface GetAppConfigCallBack {
        void callback(String jsonStr);
    }

    public GetAppConfigTask(Context context,
                            GetAppConfigCallBack getAppConfigCallBack) {
        this.context = context;
        this.getAppConfigCallBack = getAppConfigCallBack;
    }

    @Override
    protected void onPreExecute() {
        if (context == null) {
            throw new NullPointerException("context为空");
        }
        appLoadDialog = new ProgressDialog(context);
        appLoadDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Socket socket = null;
        OutputStream os = null;
        DataInputStream is = null;
//        DataInputStream dis = null;
        try {
            // 1.socket请求
            socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(50000);
            os = socket.getOutputStream();

//            dis = new DataInputStream(is);
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

            is = new DataInputStream(socket.getInputStream());

            // 2.socket响应
            String s = "";
            int contentLength = 5;

            HashMap<String, String> map = SocketStreamUtil.getHttpHead(is);
            if (map.get("Content-Length") != null) {
                try {
                    contentLength = Integer.valueOf(map
                            .get("Content-Length"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, map.get("httpHead"));
            Log.e(TAG, "contentLength:" + contentLength);
            byte[] body = SocketStreamUtil.getHttpBody(is, contentLength);
            String charset = map.get("charset");
            Log.v(TAG, "len:" + body.length);
            s = new String(body, charset != null ? charset : "GBK");
            Log.v(TAG, "消息体：" + s);

            jsonData = s;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (appLoadDialog != null) {
            appLoadDialog.dismiss();
        }
        getAppConfigCallBack.callback(jsonData);
        super.onPostExecute(result);
    }
}
