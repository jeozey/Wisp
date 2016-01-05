package com.rj.wisp.task;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.bean.HttpPkg;
import com.rj.wisp.core.Commons;
import com.rj.wisp.core.LocalSocketRequestTool;

/**
 * 作者：志文 on 2016/1/4 0004 16:42
 * 邮箱：594485991@qq.com
 */
public class HandWrittingTask extends AsyncTask<String, Integer, String[]> {
    private static final String TAG = HandWrittingTask.class.getName();
    private Handler handler;

    public HandWrittingTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.sendEmptyMessage(HandlerWhat.SHOW_LOADING);
    }

    @Override
    protected String[] doInBackground(String... params) {
        String callBack = params[0];
        String pngUrl = params[1];
        String hwUrl = null;
        if (params.length == 3) {
            hwUrl = params[2];
        }
        LocalSocketRequestTool localSocketRequestTool = new LocalSocketRequestTool();
        HttpPkg pngHttpPkg = localSocketRequestTool.getLocalSocketRequest("GET " + pngUrl + " HTTP/1.1", null);

        if (pngHttpPkg != null) {
            String pngPath = pngHttpPkg.getHead().get(Commons.File_Path);
            if (hwUrl != null) {
                HttpPkg hwHttpPkg = localSocketRequestTool.getLocalSocketRequest("GET " + hwUrl + " HTTP/1.1", null);
                if (hwHttpPkg != null) {
                    String hwPath = hwHttpPkg.getHead().get(Commons.File_Path);
                    return new String[]{callBack, pngPath, hwPath};
                } else {
                    return null;
                }
            } else {
                return new String[]{callBack, pngPath};
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String[] s) {
        super.onPostExecute(s);
        handler.sendEmptyMessage(HandlerWhat.DISMISS_LOADING);
        if (s == null || s.length == 0) {
            Log.e(TAG, "下载手写图片出错");
        } else {
            Message msg = handler.obtainMessage(HandlerWhat.HANDWRITING_OPEN_FILE);
            msg.obj = s;
            handler.sendMessage(msg);
        }
    }
}
