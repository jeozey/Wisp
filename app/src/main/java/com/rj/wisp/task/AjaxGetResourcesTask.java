package com.rj.wisp.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rj.framework.DB;
import com.rj.framework.download.DownLoadResourcePool;
import com.rj.framework.download.DownLoadResourcesThread;
import com.rj.view.ToastTool;
import com.rj.wisp.core.SocketStreamUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * 下载资源文件
 */
public class AjaxGetResourcesTask extends AsyncTask<Object, Void, String> {
    private static final String TAG = AjaxGetResourcesTask.class.getName();
    private String jsonData = null;
    private Activity activity = null;
    private JSONArray jsonArray = null;
    private Handler outHandler;
    private Boolean stop = false;
    public static HashMap<String, String[]> sourceMap = new HashMap<String, String[]>();// 资源下载

    public AjaxGetResourcesTask(Activity activity, Handler outHandler) {
        this.activity = activity;
        this.outHandler = outHandler;
    }

    private int faildNum = 0;
    private ProgressDialog downLoadDialog;
    private final int downloadPoolNum = 5;// 下载任务池大小
    private int count = 0;

    public void sendMsg(int what, Object data) {
        try {
            if (!stop && !Thread.currentThread().isInterrupted()) {
                switch (what) {
                    case 0:
                        count++;
                        downLoadDialog.setProgress(count);
                        String[] d = (String[]) data;
                        sourceMap.put(d[0], new String[]{d[1], d[2]});
                        Log.e("test4", "下载成功 count " + count + " " + sourceMap.size() + " " + d[0]);
                        if (count == downLoadDialog.getMax()) {
                            downLoadDialog.cancel();
                            if (faildNum == 0) {
                                // ToastTool.show(context, "缓存文件更新成功", 1);
                            } else {
                                ToastTool.show(activity, "下载失败:" + faildNum
                                        + "个缓存文件", 1);
                            }

                            Log.e("EEEEE", "MAP SIZE" + sourceMap.size());

                            SourceFileUtil.writeResourceJson(sourceMap,
                                    resourceJsonPath);
                            Message msg = new Message();
                            msg.what = 10;
                            msg.arg1 = faildNum;
                            outHandler.sendMessage(msg);
                        } else {
                            checkNextResources(count - 1);
                        }
                        break;
                    case 1:// 下载失败F
                        count++;
                        downLoadDialog.setProgress(count);
                        faildNum++;
                        Log.e("test4", "下载失败 count " + count);
                        if (count == downLoadDialog.getMax()) {
                            downLoadDialog.cancel();
                            if (faildNum == 0) {
                                // ToastTool.show(context, "缓存文件更新成功", 1);
                            } else
                                ToastTool.show(activity, "下载失败:" + faildNum
                                        + "个缓存文件", 1);
                            SourceFileUtil.writeResourceJson(sourceMap,
                                    resourceJsonPath);
                            Message msg = new Message();
                            msg.what = 10;
                            msg.arg1 = faildNum;
                            outHandler.sendMessage(msg);
                        } else {
                            checkNextResources(count - 1);
                        }
                        break;
                    case 2:// 下载完成
                        if (downLoadDialog != null) {
                            downLoadDialog.cancel();
                        }
                        if (faildNum == 0) {
                            // ToastTool.show(context, "缓存文件更新成功", 1);
                        } else {
                            ToastTool.show(activity, "下载失败:" + faildNum + "个缓存文件",
                                    1);
                        }
                        Log.e("web 开始载入： ： ", "11111");
                        Message msg = new Message();
                        msg.what = 10;
                        msg.arg1 = faildNum;
                        outHandler.sendMessage(msg);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected String doInBackground(Object... params) {
        getAllSource();

        return "done";
    }

    private void getAllSource() {
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            socket = new Socket(DB.HTTPSERVER_HOST, DB.HTTPSERVER_PORT);
            socket.setSoTimeout(20000);
            // socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
            socket.setSoLinger(true, 0);// socket关闭时，立即释放资源
            socket.setTrafficClass(0x04 | 0x10);// 高可靠性和
            os = socket.getOutputStream();
            is = socket.getInputStream();

            os.write(("GET /wisp_aas/adapter?open&_method=getResourcesList&appcode="
                    + DB.APP_CODE + " HTTP/1.1" + "\r\n").getBytes());
            os.write(("user-agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1;"
                    + DB.USER_AGENT + ")\r\n").getBytes());
            os.write(("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n")
                    .getBytes());
            os.write(("Accept-Language: zh-CN, en-US" + "\r\n").getBytes());
            os.write(("Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
                    + "\r\n").getBytes());
            os.write(("Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7"
                    + "\r\n").getBytes());
            os.write("\r\n".getBytes());
            os.flush();
            String s = "";

            int contentLength = 0;
            Map<String, String> map = SocketStreamUtil.readHeaders(is);
            if (map.get("WISP-Content-Length") != null) {
                try {
                    contentLength = Integer.valueOf(map
                            .get("WISP-Content-Length"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            byte[] buf = {};
            int size = 0;
            if (contentLength != 0) {
                buf = new byte[contentLength];
                while (size < contentLength) {
                    int c = is.read();
                    buf[size++] = (byte) c;
                }
                s = new String(buf, 0, size, "GBK");
                Log.e("RJMOA", "消息体：" + s);
            }
            jsonData = s;

        } catch (Exception e) {
            e.printStackTrace();
            outHandler.sendEmptyMessage(-4);// mq:
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }


    @Override
    protected void onPostExecute(String result) {
        try {
            if (result == null) {
                return;
            }
            this.getData();
//			Message msg = Message.obtain();
//			msg.what = 11;
//			msg.obj = jsonData;
//			outHandler.sendMessage(msg);
        } catch (Exception e) {
            outHandler.sendEmptyMessage(-4);// mq:
            e.printStackTrace();
        }
        super.onPostExecute(result);
    }

    private void showProgressDialog(boolean isFirst) {
        System.out.println("showProgressDialog");
        if (activity != null) {
            downLoadDialog = new ProgressDialog(activity);
            downLoadDialog.setTitle("资源下载");
            downLoadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            downLoadDialog.setIndeterminate(false);
            downLoadDialog.setCancelable(false);
            downLoadDialog.setButton(DialogInterface.BUTTON_POSITIVE, "重试",
                    new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            resetButtonMethod();
                        }
                    });

            downLoadDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消",
                    new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            cancelButtonMethod();
                        }
                    });
        }
        if (isFirst) {
            downLoadDialog.show();
        }
    }

    /**
     * mq:下载资源：重试按钮，方法
     */
    private void resetButtonMethod() {
        SourceFileUtil.writeResourceJson(sourceMap,
                resourceJsonPath);
        faildNum = 0;
        stop = true;
        sourceMap = null;
        if (downLoadDialog == null)
            return;
        downLoadDialog.cancel();
        downLoadDialog.dismiss();
        if (outHandler != null) {
            if (downLoadResource != null) {
                downLoadResource.stop();
            }
            downLoadDialog.cancel();
            downLoadDialog.dismiss();
            outHandler.sendEmptyMessage(8);
        }
    }

    /**
     * mq:下载资源：取消按钮，方法
     */
    private void cancelButtonMethod() {
        SourceFileUtil.writeResourceJson(sourceMap,
                resourceJsonPath);
        stop = true;
        if (downLoadDialog != null) {
            downLoadDialog.cancel();
            downLoadDialog.dismiss();
        }
    }

    /**
     * 下载资源时出错，处理机制
     */
    public void downResExcept() {
        if (downLoadDialog == null)
            return;
        cancelButtonMethod();// mq:在下载失败之前，先处理取消下载资源
        resetButtonMethod();// mq:突然断网后，重新驱动，下载资源
    }

    private final String resourceJsonPath = DB.RESOURCE_PATH + "sourcelist.txt";

    private List<Resources> getData() throws Exception {
        System.out.println("getData()");
        SourceFileUtil.isWriting = false;
        stop = false;
        count = 0;
        List<Resources> list = new ArrayList<Resources>();
        if (jsonData != null) {
            Log.e("resource", "jsonData:" + jsonData);
            boolean isExit = true;
            JSONArray loadsJsonArray = new JSONArray();
            jsonArray = new JSONArray(jsonData); // 服务器获取到的资源文件列表

            // 这里打开资源清单文件，并保存为JSONArray
            // 如果清单文件不存在说明需要重新下载资源
            File sourceList = new File(resourceJsonPath);

            sourceMap = new HashMap<String, String[]>();
            if (sourceList.exists() && sourceList.length() != 0) {
                sourceMap = SourceFileUtil.read(sourceList);
            }

            // for循环遍历资源文件列表，与清单文件比对，找出需要下载的资源文件
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    Object obj = jsonArray.get(i);
                    if (obj == null) {
                        Log.e(TAG, "资源文件为空:" + i);
                        continue;
                    }
                    JSONObject jsonObject = (JSONObject) obj;
                    isExit = checkIsExit(jsonObject);
                    // Log.v("资源", isExit + ":" + i);
                    if (!isExit) {
                        loadsJsonArray.put(jsonObject);
                        isExit = true;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "资源文件为空:" + e.getLocalizedMessage());
                    e.printStackTrace();
                }

            }
            jsonArray = loadsJsonArray;
            if (jsonArray.length() > 0) {
//				Log.v("资源", "新资源" + jsonArray.length());
//				Message msg = new Message();
//				msg.what = 11;
//				msg.obj = jsonArray.length();
//				outHandler.sendMessage(msg);

                showProgressDialog(true);
                if (downLoadDialog != null) {
                    downLoadDialog.setMax(jsonArray.length());
                }
                System.out.println("downloadNextResources(0);");
                downloadNextResources(0);
                // downLoadResources(jsonArray);
            } else {
                Log.v("资源", "没有新资源");
                if (downLoadDialog != null) {
                    downLoadDialog.cancel();
                }
                outHandler.sendEmptyMessage(9);
            }
        }
        return list;
    }

    private void downloadNextResources(int number) {
        if (jsonArray != null) {
            if (jsonArray.length() >= (number + 1) * downloadPoolNum)//
            { // by 江志文
                checkNextResources(number * downloadPoolNum, (number + 1)
                        * downloadPoolNum);
            } else { // by 江志文
                checkNextResources(number * downloadPoolNum, jsonArray.length());
            }
        }

    }

    private void checkNextResources(int begin, int end) {
        try {
            for (int i = begin; i < end; i++) {
                if (stop) {
                    break;
                }
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                Log.e("NNN", "jsonObject = " + jsonObject);
                try {
                    checkResources(jsonObject.getString("filepath"),
                            jsonObject.getString("filetype"),
                            jsonObject.getString("modified"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 检查资源是否存在
     *
     * @param jsonObject
     * @return true 资源存在， false 为不存在
     * @throws Exception
     */
    private boolean checkIsExit(JSONObject jsonObject) throws Exception {
        // if(sourceMap == null){
        // return false;
        // }
        String filepath = "";
        String modified = "";
        String filetype = "";
        if (!jsonObject.isNull("filepath")) {
            filepath = jsonObject.getString("filepath");
        }
        if (!jsonObject.isNull("filetype")) {
            filetype = jsonObject.getString("filetype");
        }
        if (!jsonObject.isNull("modified")) {
            modified = jsonObject.getString("modified");
        }

        String localtype = "";
        String localmodified = "";

        String path = DB.RESOURCE_PATH + filepath;
        File f = new File(path);
        if (!"folder".equals(filetype) && !f.exists()) {
            return false;
        }

        String[] tmp = sourceMap.get(path);
        // if(tmp == null){
        // return false;
        // }
        if (tmp != null) {
            localtype = tmp[0];
            localmodified = tmp[1];
        }

        boolean result = true;
        if ("".equals(localtype) && "".equals(localmodified)) {
            if ("folder".equals(filetype)) {// 文件夹类型 创建新文件夹
                File file = new File(DB.RESOURCE_PATH
                        + filepath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                result = true;
            } else {
                result = false;
            }

        } else {
            if (!modified.equals(localmodified)) {// 修改日期不一致 以服务器时间为准 修改
                Log.e("test1", "filepath: " + filepath + "  " + modified + " "
                        + localmodified);
                result = false;
            }
        }
        return result;
    }

    private void checkNextResources(int index) {
        if (index >= jsonArray.length())
            return;
        try {
            JSONObject jsonObject = (JSONObject) jsonArray.get(index);
            try {
                checkResources(jsonObject.getString("filepath"),
                        jsonObject.getString("filetype"),
                        jsonObject.getString("modified"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkResources(String filepath, String filetype,
                                String modified) throws IOException {
        new DownLoadResourcesThread(filepath, filetype, modified).start();
//		Message msg = Message.obtain();
//		msg.what = 11;
//		Bundle data = new Bundle();
//		data.putString("filepath", filepath);
//		data.putString("filetype", filetype);
//		data.putString("modified", modified);
//		msg.setData(data);
//		outHandler.sendMessage(msg);
    }

    private DownLoadResourcePool downLoadResource;
}