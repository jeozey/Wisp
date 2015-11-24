package com.rj.framework.download;

import android.util.Log;

import com.rj.framework.DB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownLoadResourcePool {
    private ExecutorService executor;
    private JSONArray array = null;
    private Boolean stop = false;
    private static final int THREAD_COUNT = 5;

    public DownLoadResourcePool(JSONArray array) {
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
        this.array = array;
    }

    public void stop() {
        try {
            if (executor != null) {
                executor.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stop = true;
    }

    public void start() {
        // MyThreadPool myThreadPool = new MyThreadPool();
        if (array == null) {
            return;
        }
        for (int c = 0; c < array.length(); c++) {
            if (stop)
                break;
            try {
                final JSONObject jsonObject = (JSONObject) array.get(c);

                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        downLoad(jsonObject);
                    }
                });
            } catch (JSONException e) {
                Log.e("test6", "线程池 " + e.getLocalizedMessage());
                e.printStackTrace();
//				AjaxGetResourcesList.sendMsg(1);// 需要通知进度条
            }

        }
    }

    private void downLoad(JSONObject jsonObject) {
        Socket socket = null;
        OutputStream os = null;
        try {

            String filepath = jsonObject.getString("filepath");
            String filetype = jsonObject.getString("filetype");
            String modified = jsonObject.getString("modified");

            socket = new Socket(DB.HTTPSERVER_HOST,
                    DB.HTTPSERVER_PORT);
            socket.setSoTimeout(60000);
            os = socket.getOutputStream();
            os.write(("GET /wisp_aas/" + filepath + " HTTP/1.1" + "\r\n")
                    .getBytes());
            os.write(("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n")
                    .getBytes());
            os.write(("Method-Type: download" + "\r\n").getBytes());
            os.write(("File-Time: " + modified + "\r\n").getBytes());
            os.write(("File-Type: " + filetype + "\r\n").getBytes());
            os.write(("File-Name: " + filepath + "\r\n").getBytes());
            os.write(("Accept: */*" + "\r\n").getBytes());
            os.write(("Accept-Encoding: gzip, deflate" + "\r\n").getBytes());
            os.write(("Accept-Language: zh-CN, en-US" + "\r\n").getBytes());
            os.write(("Connection: Keep-Alive" + "\r\n").getBytes());
            os.write("\r\n".getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
//			AjaxGetResourcesList.sendMsg(1);// 需要通知进度条
            Log.e("test6", "socket 开太多了" + e.getLocalizedMessage());
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (Exception e2) {
                    // TODO: handle exception
                }
        }
    }

}
