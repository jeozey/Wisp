package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.rj.connection.ISocketConnection;
import com.rj.framework.DB;
import com.rj.util.FileUtil;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.bean.HttpPkg;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 作者：志文 on 2015/11/18 0018 15:19
 * 邮箱：594485991@qq.com
 */
public class ServiceThread extends Thread {
    private static final String TAG = ServiceThread.class.getName();

    private Socket webViewSocket;
    private Handler handler;
    private DataInputStream webView_reader;
    //    private BufferedReader webView_reader;
    private OutputStream webView_os;
    private Context context;

    public ServiceThread(Socket socket, Handler handler, Context context) {
//        Log.e(TAG, "socket:" + socket);
        this.webViewSocket = socket;
        this.handler = handler;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        try {
            Log.e(TAG, "run...");
            if (webViewSocket != null) {
                HttpPkg httpPkg = getWebViewRequest();
                if (httpPkg == null) {
                    return;
                }
                Log.v(TAG, "httpPkg content-length:" + httpPkg.getHead().get(Commons.Content_Length));
//                Log.e(TAG, "httpPkg.getHead():" + httpPkg.getHead());
                handleHttpPkg(httpPkg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                webView_reader.close();
            } catch (Exception e) {

            }
            try {
                webView_os.close();
            } catch (Exception e) {

            }
            try {
                webViewSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 获取用户请求数据，消息头首行
//    private String getHeadLine() {
//
//        try {
//            Reader reader = new InputStreamReader(webViewSocket.getInputStream());
//            webView_reader = new BufferedReader(reader, 1024);
//            webView_os = webViewSocket.getOutputStream();
//            String head_line = webView_reader.readLine();
//            Log.e(TAG, "head_line:" + head_line);
//            return head_line;
//        } catch (IOException e1) {
//            e1.printStackTrace();
//            Log.e(TAG, "请求第一行出错:" + e1.getMessage());
//
//        }
//        return null;
//    }


    private void handleHttpPkg(HttpPkg httpPkg) {
        try {
            String head_line = httpPkg.getHeadLine();
            if (TextUtils.isEmpty(head_line)) {
                Log.e(TAG, "head_line is null");
                return;
            }
            Log.e(TAG, "handleHttpPkg:" + head_line);
            if (head_line.indexOf("/config/html/") != -1) {// 资源类下载
                if ("down".equals(httpPkg.getHead().get("Method-Type"))) {
                    downResource(httpPkg, false);
                } else {
                    getResource(httpPkg);
                }
            } else if (head_line.indexOf("AjAxSocketIFC") != -1) {
                // ajax请求
                handleAjaxRequest(httpPkg);
            } else if (head_line.indexOf("@@LocalSocket") != -1) {
                handleLocalSocketRequest(head_line);
            } else {
                httpRequest(httpPkg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocalSocketRequest(String head_line) throws Exception {
        if (head_line.indexOf("checkConnection") != -1) {
            checkConnection();
        } else if (head_line.indexOf("versionUpdate") != -1) {
            versionUpdate();
        }
    }

    private void handleAjaxRequest(HttpPkg httpPkg) throws Exception {
        String head_line = httpPkg.getHeadLine();
        Log.e(TAG, "handleAjaxRequest:" + head_line);
        //需要有返回给webview,否则ajax会等待超时报错
        responseEmptyToWebView();
        // 同步界面组件添加
        if (head_line.indexOf("addWebUI") != -1) {
            addWebUi(httpPkg);
        } else if (head_line.indexOf("addWebBtnNum") != -1) {
            addWebBtnNum();
        } else if (head_line.indexOf("@@ShowProgressDialog@@") != -1) {
            showProgressDialog();
        } else if (head_line.indexOf("@@DismissProgressDialog@@") != -1) {
            dismissProgressDialog();
        }
    }



    private void responseEmptyToWebView() {
        responseWebView("HTTP/1.0 200 OK\r\n", "".getBytes());
    }

    private void addWebBtnNum() {
    }

    private void showProgressDialog() throws IOException {

        Log.e(TAG, "dismissProgressDialog:" + handler);
        if (handler != null) {
            Message msg = handler.obtainMessage(HandlerWhat.SHOW_LOADING);
            handler.sendMessage(msg);
        }
    }

    private void dismissProgressDialog() throws IOException {
        Log.e(TAG, "dismissProgressDialog:" + handler);
        if (handler != null) {
            Message msg = handler.obtainMessage(HandlerWhat.DISMISS_LOADING);
            handler.sendMessage(msg);

        }
    }

    private static final String CHAR_SET = "utf-8";
    private static ArrayList<String> charsets = new ArrayList<String>();

    static {
        charsets.add("gbk");
        charsets.add("gb2312");
        charsets.add("utf8");
        charsets.add("utf-8");
    }

    private String getCharSet(HttpPkg httpPkg) {
        try {
            String contentType = httpPkg.getHead().get(Commons.CONTENT_TYPE);
            int i = contentType.indexOf("charset=");
            if (i != -1) {
                String c = contentType.substring(i + 8);
                if (charsets.contains(c.toLowerCase())) {
                    Log.e(TAG, "c:" + c);
                    return c;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CHAR_SET;

    }

    private void addWebUi(HttpPkg httpPkg) throws IOException {
        Log.e(TAG, "addWebUi:" + handler);
        if (handler != null) {
            String jsonStr = new String(httpPkg.getBody(), getCharSet(httpPkg));

            Log.v(TAG, "addWebUi:" + jsonStr);
            Message msg = handler.obtainMessage(HandlerWhat.ADD_WEB_UI);
            msg.obj = jsonStr;
            handler.sendMessage(msg);

        }
    }

    private void getResource(HttpPkg httpPkg) throws Exception {
        String head_line = httpPkg.getHeadLine();
        Log.v(TAG, "获取资源:" + head_line);

        String filename = head_line.substring(
                head_line.indexOf("config/html"), head_line.length() - 9);
        File filePath = new File(DB.RESOURCE_PATH
                + filename);
//        Log.e(TAG, "filePath:" + filePath.getAbsolutePath());
        try {
            //缓存被删后，重新下载 并写入webview
            if (filePath.exists()) {
//                Log.e(TAG, "resource file exist");
                writeCacheToWebView(filename);
            } else {
//                Log.e(TAG, "resource file not exist");
                downResource(httpPkg, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (filePath != null && filePath.exists()) {
                filePath.delete();
            }
        }
    }

    private void downResource(HttpPkg httpPkg, boolean writeFile) throws Exception {
        String head_line = httpPkg.getHeadLine();


        Log.v(TAG, "下载资源:" + head_line);

        // 对中间件发送数据包
        try {

            HttpPkg pkg = sendRequest(httpPkg.getHead().get(Commons.HTTP_HEAD), null);


            HashMap<String, String> head_sb = pkg.getHead();
            Log.e(TAG, "head_sb:" + "@" + head_line + "@" + head_sb);

            if (head_sb.get(Commons.HTTP_HEAD).indexOf(Commons.NOT_FOUND) == -1) {
                byte[] body = pkg.getBody();
                if (body != null) {

                    Log.e(TAG, "body.length:" + body.length);
//                    responseWebView(head_sb.get(Commons.HTTP_HEAD), body);

                    Log.e(TAG, "writeFile:" + writeFile);
                    if (writeFile) {
                        String filename = head_line.substring(
                                head_line.indexOf("config/html"), head_line.length() - 9);
                        File file = new File(DB.RESOURCE_PATH
                                + filename);
                        FileUtil.writeFile(file, body);
                    }
//
//            Log.e(TAG, "下载完毕,发送通知");
//            //通知订阅者下载完一个资源
//            EventBus.getDefault().post(new ResourceMessageEvent(ResourceMessageEvent.RESOURCE_DOWN_SUCC, filename));
                }
            } else {
                responseEmptyToWebView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void writeCacheToWebView(String requestfilename)
            throws Exception {
        Log.e("cache", "高速缓存：" + requestfilename);
        byte[] content = new byte[20480];
//		File cacheFileHead = null;
        File cacheFile = null;
//        webView_os = webViewSocket.getOutputStream();
        FileInputStream security_is = null;
        try {
            int j = 0;

            cacheFile = new File(DB.RESOURCE_PATH
                    + requestfilename);

            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.0 200 OK\r\n");//返回应答消息,并结束应答
            sb.append("File-Path:" + requestfilename + "\r\n");//返回应答消息,并结束应答
//			sb.append("Content-Type: application/octet-stream\r\n".getBytes());
            sb.append(("Content-Length: " + cacheFile.length() + "\r\n"));// 返回内容字节数

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len = 0;
            security_is = new FileInputStream(cacheFile);
            while ((j = security_is.read(content)) != -1) {
                byteArrayOutputStream.write(content, 0, j);
                len += j;
            }

            responseWebView(sb.toString(), byteArrayOutputStream.toByteArray());
            Log.e(TAG, "cacheFile.length():" + cacheFile.length());
            Log.e(TAG, "file len:" + len);
            security_is.close();

//            webView_os.write(byteArrayOutputStream.toByteArray());
            webView_os.close();
            Log.e(TAG, "缓存写入完成" + cacheFile.getAbsolutePath());
//        } catch (FileNotFoundException e0) {
//            Log.e(TAG, "资源文件不存在:" + cacheFile.getAbsolutePath());
//			e0.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "第一次缓存写入失败");
            // 打开缓存文件失败。。。。怎么办？ 当然 按原始的方法 去请求
//            writeCacheToWebViewTwice(head_line);
        }
    }




    /**
     * 1.获取用户请求数据(无GZIP压缩)
     *
     * @return HttpPkg
     * @throws IOException
     */
    private HttpPkg getWebViewRequest() throws IOException {

        try {
            HttpPkg pkg = new HttpPkg();
//            Reader reader = new InputStreamReader(webViewSocket.getInputStream());
//            webView_reader = new BufferedReader(reader, 1024);
            webView_reader = new DataInputStream(webViewSocket.getInputStream());

            String firstLine = webView_reader.readLine();

            Log.e(TAG, "firstLine:" + firstLine);
            pkg.setHeadLine(firstLine);
            HashMap<String, String> map = SocketStreamUtil.getHttpHead(webView_reader);
            String head = map.get(Commons.HTTP_HEAD);
            if (!TextUtils.isEmpty(head)) {
                map.put(Commons.HTTP_HEAD, firstLine + Commons.CRLF_STR + head);
            }
            pkg.setHead(map);

//            if(firstLine.indexOf("AjAxSocketIFC")!=-1){
//                return pkg;
//            }

            String contentLength = map.get(Commons.Content_Length);
            if (!TextUtils.isEmpty(contentLength)) {

                int len = Integer.valueOf(contentLength);
                if (len > 0) {
                    Log.e(TAG, "contentLength:" + contentLength);
//                byte[] body = SocketStreamUtil.getHttpBody(webViewSocket.getInputStream(), len);
                    byte[] body = SocketStreamUtil.getHttpBody(webView_reader, len);
                    Log.e(TAG, "contentLength:" + contentLength + " body.length:" + body.length);
                    pkg.setBody(body);
                }
            }
            Log.e(TAG, "getWebViewRequest over:" + firstLine);
            return pkg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 普通http请求
     *
     * @param httpPkg
     * @throws Exception
     */

    public void httpRequest(HttpPkg httpPkg) throws Exception {

        //这边还得补上keyInfo、userAgent等信息
        Log.v("request", "普通:" + httpPkg.getHeadLine());

//        byte[] head = null;
//        byte[] body = null;
//        if(httpPkg.getHead()!=null){
//            head = GzipUtil.byteCompress(httpPkg.getHead().get(Commons.HTTP_HEAD).getBytes(), DB.IS_GZIP);
////            head = GzipUtil.byteCompress((httpPkg.getHead().get(Commons.HTTP_HEAD)+Commons.CRLF_STR).getBytes(), DB.IS_GZIP);
//        }
//        if(httpPkg.getBody()!=null){
//            body = GzipUtil.byteCompress(httpPkg.getBody(), DB.IS_GZIP);
//        }
//        sendRequest(head, body);
        sendRequest(httpPkg.getHead().get(Commons.HTTP_HEAD), httpPkg.getBody());
        Log.e(TAG, "httpRequest 请求数据发起成功:");
    }

    public void checkConnection() {
        Log.e(TAG, "checkConnection");
        try {
            String head = "GET /wisp_aas/adapter?open&_method=checkConnection&appcode=" + DB.APP_CODE + Commons.CRLF_STR;
            HttpPkg httpPkg = sendRequest(head, null);
            Log.e(TAG, "checkConnection:" + httpPkg.getHead());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkConnection write over");
    }

    public void versionUpdate() {
        Log.e(TAG, "checkConnection");
        try {
            String head = ("SOCKET /AjAxSocketIFC/versionUpdate/android" + Commons.CRLF_STR + "Content-Length: "
                    + DB.APP_VERSION_ID.length() + Commons.CRLF_STR);
            byte[] body = DB.APP_VERSION_ID
                    .getBytes();

            Log.v(TAG, "更新:" + DB.APP_VERSION_ID);
            Log.v(TAG, "更新:" + new String(head));
            sendRequest(head, body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkConnection write over");
    }

    private void checkLoginOut(String head) {
        if (!TextUtils.isEmpty(head) && head.indexOf("LoginPage") != -1) {
            Log.e(TAG, "注销: ");
            // 页面为 登陆页 超时注销
            Message msg = handler.obtainMessage(HandlerWhat.LOGIN_PAGE);
            handler.sendMessage(msg);
        }
    }

    private HttpPkg sendRequest(String head, byte[] body) {
        Log.e(TAG, "sendRequest");
        ISocketConnection connection = SocketFactory.getSSLSocket();

        Log.e(TAG, "sendRequest write begin");
        try {
            if (head != null) {
                connection.write(head.getBytes());

                if (head.endsWith(Commons.CRLF2_STR)) {

                } else if (head.endsWith(Commons.CRLF_STR)) {
                    connection.write(Commons.CRLF_BYTE);
                } else {
                    connection.write(Commons.CRLF2_BYTE);
                }
            }

            if (body != null) {
                if (head.indexOf("config/html") == -1) {
                    Log.d(TAG, "sendRequest to server:" + new String(body));
                }
                connection.write(body);
            }
            //server socket InputStream read()==-1 标识流结束
            connection.shutDownOutPut();

            HashMap<String, String> map = connection.getHttpHead2();
            Log.e(TAG, "get http response:" + map);
            String contentLength = map.get(Commons.Content_Length);
            byte[] content = null;
            if (!TextUtils.isEmpty(contentLength)) {
                int len = Integer.valueOf(contentLength);
                content = connection.getHttpBody(len);
                Log.e(TAG, "get server content over:" + (content != null ? content.length : 0));
                responseWebView(map.get(Commons.HTTP_HEAD), content);
            } else {
                Log.e(TAG, "get server content over");
                responseWebView(map.get(Commons.HTTP_HEAD), null);
            }
            return new HttpPkg(map, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "sendRequest write over");
        return null;
    }

    private void responseWebView(String head, byte[] body) {
        try {
            Log.e(TAG, "socket:" + webViewSocket);
            webView_os = webViewSocket.getOutputStream();

            //是否为注销页面（影响效率,考虑是否在webview直接判断）
            checkLoginOut(head);

//            Log.e("responseWebView", "head123:" + new String(head));
            if (head != null) {
                webView_os.write(head.getBytes());

                if (head.endsWith(Commons.CRLF2_STR)) {

                } else if (head.endsWith(Commons.CRLF_STR)) {
                    webView_os.write(Commons.CRLF_BYTE);
                } else {
                    webView_os.write(Commons.CRLF2_BYTE);
                }

            }


            if (body != null) {
//                if(head.indexOf("config/html")==-1) {
//                    Log.e(TAG, "response to web:" + new String(body));
//                }
                webView_os.write(body);
            }
            webView_os.close();
            Log.e("responseWebView", "responseWebView over");

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "error:" + head);
        }
    }


}
