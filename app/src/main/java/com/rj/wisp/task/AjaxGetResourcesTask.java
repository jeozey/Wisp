package com.rj.wisp.task;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.rj.framework.DB;
import com.rj.util.FileUtil;
import com.rj.wisp.bean.HttpPkg;
import com.rj.wisp.bean.ResourceConfigSaveEvent;
import com.rj.wisp.bean.ResourceFile;
import com.rj.wisp.bean.ResourceMessageEvent;
import com.rj.wisp.core.Commons;
import com.rj.wisp.core.LocalSocketRequestTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/*
 * 下载资源文件
 */
public class AjaxGetResourcesTask extends AsyncTask<String, Void, String> {
    private static final String TAG = AjaxGetResourcesTask.class.getName();
    private final String resourceJsonPath = DB.RESOURCE_PATH + "sourcelist.txt";
    private String jsonData = null;

    private List<ResourceFile> remoteResources = new ArrayList<>();
    private Map<String, ResourceFile> localResources = new HashMap<>();
    private Map<String, ResourceFile> needDownLoadResources = new HashMap<>();
    private Map<String, ResourceFile> downFailResources = new HashMap<>();

    public void onEvent(ResourceConfigSaveEvent event) {
        saveConfigFile();
    }

    public AjaxGetResourcesTask() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected String doInBackground(String... params) {
//        return getAllSource();
//        return params[0];


        final String result = params[0];
        if (result != null) {
            try {
                getData(result);
            } catch (JSONException e) {
                e.printStackTrace();
                new File(resourceJsonPath).delete();
                EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_CONFIG_FORMAT_FAIL, null));
            } catch (Exception e) {
                e.printStackTrace();
                EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_GET_FAIL, null));
            }
        } else {
            EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_GET_FAIL, null));

        }

        return null;
    }

//    private String getAllSource() {
//        try {
//            StringBuilder sb = new StringBuilder();
//            sb.append("GET /wisp_aas/adapter?open&_method=getResourcesList&appcode="
//                    + DB.APP_CODE + " HTTP/1.1" + "\r\n");
//            sb.append("user-agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1;"
//                    + DB.USER_AGENT + ")\r\n");
//            sb.append("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n");
//            sb.append("Accept-Language: zh-CN, en-US" + "\r\n");
//            sb.append(
//                    "Accept: application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
//                            + "\r\n");
//            sb.append("Accept-Charset: utf-8, iso-8859-1, utf-16, *;q=0.7"
//                    + "\r\n");
//
//            HttpPkg httpPkg = new LocalSocketRequestTool().getLocalSocketRequest(sb.toString().getBytes(), null);
//
//            byte[] content = httpPkg.getBody();
//            String charset = httpPkg.getHead().get("charset");
//            jsonData = new String(content, charset != null ? charset : "GBK");
//            return jsonData;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    @Override
    protected void onPostExecute(final String result) {
        super.onPostExecute(result);
    }


    private void checkNeedDown(ResourceFile remote, ResourceFile local) {
        if (local == null) {
            checkFileOrFolder(remote);
        } else {
            compareRemoteAndLocal(remote, local);
        }
    }

    private void checkFileOrFolder(ResourceFile remote) {
//        Log.e(TAG, "checkFileOrFolder:" + remote.getFiletype());
        if ("folder".equals(remote.getFiletype())) {
            File file = new File(DB.RESOURCE_PATH
                    + remote.getFilepath());
            if (!file.exists()) {
                file.mkdirs();
            }
        } else {
            needDownLoadResources.put(remote.getFilepath(), remote);
        }
    }

    private void compareRemoteAndLocal(ResourceFile remote, ResourceFile local) {
        File file = new File(DB.RESOURCE_PATH
                + remote.getFilepath());
        if (file.exists()) {
            //修改时间不一致
            if (!remote.getFilemodified().equals(local.getFilemodified())) {
                needDownLoadResources.put(remote.getFilepath(), remote);
            }
        } else {
            //本地文件不存在
            needDownLoadResources.put(remote.getFilepath(), remote);
        }
    }

    private void getData(String jsonData) throws Exception {
        Log.e(TAG, "getData:" + jsonData);
        SourceFileUtil.isWriting = false;
        if (jsonData != null) {

            // 这里打开资源清单文件，并保存为JSONArray
            // 如果清单文件不存在说明需要重新下载资源
            File sourceConfigFile = new File(resourceJsonPath);

            remoteResources = com.alibaba.fastjson.JSON.parseArray(jsonData, ResourceFile.class);
            if (sourceConfigFile.exists() && sourceConfigFile.length() != 0) {
                String str = FileUtil.readFile(sourceConfigFile);
                List<ResourceFile> local = com.alibaba.fastjson.JSON.parseArray(str, ResourceFile.class);
                for (ResourceFile res : local
                        ) {
                    localResources.put(res.getFilepath(), res);
                }
                Log.e(TAG, "localResources:" + (localResources != null ? localResources.size() : 0));
            }
//            List<ResourceFile>remoteResources = com.alibaba.fastjson.JSON.parseArray(jsonData, ResourceFile.class);
            Log.e(TAG, "remoteResources:" + (remoteResources != null ? remoteResources.size() : 0));

            // for循环遍历资源文件列表，与清单文件比对，找出需要下载的资源文件
            for (ResourceFile remote : remoteResources) {
                ResourceFile local = localResources.get(remote.getFilepath());
                checkNeedDown(remote, local);
            }
            Log.e(TAG, "needDownLoadResources:" + needDownLoadResources.size());
            if (needDownLoadResources.size() > 0) {
                downResource(needDownLoadResources);
            } else {
                EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_NO_UPDATE, null));
            }
        }
    }

    private Object object = new Object();

    private int hasDownCount = 0;
    //订阅消息
    public void onEvent(ResourceMessageEvent event) {
        Log.e(TAG, "onEvent ResourceMessageEvent:" + (event.getEventContent() != null ? event.getEventContent() : ""));
        if (event != null) {
            switch (event.getEventType()) {
                case ResourceMessageEvent.RESOURCE_DOWN_SUCC:
                case ResourceMessageEvent.RESOURCE_DOWN_FAIL:
                    synchronized (object) {
                        String fileName = event.getEventContent().toString();
                        if (!TextUtils.isEmpty(fileName)) {
                            ResourceFile resourceFile = needDownLoadResources.get(fileName);
                            if (resourceFile != null) {
                                if (ResourceMessageEvent.RESOURCE_DOWN_FAIL == event.getEventType()) {
                                    downFailResources.put(fileName, resourceFile);
                                }
                                localResources.put(fileName, resourceFile);
                                hasDownCount++;
                                Log.e(TAG, "needDownLoadResources.size():" + needDownLoadResources.size());
                                if (hasDownCount == needDownLoadResources.size()) {
                                    saveConfigFile();
                                    EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_END, downFailResources.size()));
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void saveConfigFile() {
        if (localResources != null && localResources.size() > 0) {
            Log.e(TAG, "write begin");
            String json = com.alibaba.fastjson.JSON.toJSONString(localResources.values());
            try {
                FileUtil.writeFile(new File(resourceJsonPath), json.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_WRITE_FAIL_FAIL, null));
            }
            Log.e(TAG, "write over");
        }
        //注销订阅
        EventBus.getDefault().unregister(this);
    }
    private void downResource(Map<String, ResourceFile> allNeedDownLoadResources) {
        //下载开始
        EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_START, allNeedDownLoadResources.size()));

        ExecutorService executor = Executors.newFixedThreadPool(5);
        Iterator iterator = allNeedDownLoadResources.entrySet().iterator();
        Log.e(TAG, "allNeedDownLoadResources.size():" + allNeedDownLoadResources.size());
        while (iterator.hasNext()) {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final ResourceFile value = (ResourceFile) entry.getValue();

            final String filePath = value.getFilepath();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    downResource(filePath);
                }
            });
        }
        executor.shutdown();

    }

    int i = 0;

    private void downResource(final String filepath) {
        StringBuilder sb = new StringBuilder();
        sb.append("GET /wisp_aas/" + filepath.replace(" ", "") + " HTTP/1.1" + "\r\n");
        sb.append("Host: 127.0.0.1:" + DB.HTTPSERVER_PORT + "\r\n");
        sb.append("Method-Type: download" + "\r\n");
//        sb.append("File-Time: " + modified + "\r\n");
//        sb.append("File-Type: " + filetype + "\r\n");
//        sb.append("File-Name: " + filepath + "\r\n");
        sb.append("Accept: */*" + "\r\n");
        sb.append("Accept-Encoding: gzip, deflate" + "\r\n");
        sb.append("Accept-Language: zh-CN, en-US" + "\r\n");
        sb.append("Connection: Keep-Alive" + "\r\n");

        HttpPkg httpPkg = new LocalSocketRequestTool().getLocalSocketRequest(sb.toString().getBytes(), null);

        String filename = filepath;

        if (httpPkg.getHead().get(Commons.HTTP_HEAD).indexOf(Commons.NOT_FOUND) == -1) {

            if (httpPkg != null && httpPkg.getBody() != null) {

                File file = new File(DB.RESOURCE_PATH
                        + filename);

                try {
                    FileUtil.writeFile(file, httpPkg.getBody());
                } catch (Exception e) {
                    e.printStackTrace();
                    //通知订阅者下载失败一个资源
                    EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_FAIL, filename));
                }

                //通知订阅者下载完成一个资源
                EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_SUCC, filename));
            } else {
                //通知订阅者下载失败一个资源
                EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_FAIL, filename));
            }
        } else {
            //通知订阅者下载失败一个资源
            EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_FAIL, filename));
        }
    }
}