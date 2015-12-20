package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.rj.connection.ISocketConnection;
import com.rj.framework.DB;
import com.rj.util.FileUtil;
import com.rj.util.GzipUtil;
import com.rj.wisp.bean.HttpPkg;
import com.rj.wisp.bean.MessageEvent;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * 作者：志文 on 2015/11/18 0018 15:19
 * 邮箱：594485991@qq.com
 */
public class ServiceThread extends Thread {
    private static final String TAG = ServiceThread.class.getName();
    private static final String HTTP_HEAD = "httpHead";
    private static final String Content_Length = "Content-Length";
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte[] CRLF = {CR, LF};
    private static final String _CRLF = "\r\n";
    private Socket webViewSocket;
    private Handler handler;
    private BufferedReader webView_reader;
    private OutputStream webView_os;
    private Context context;

    public ServiceThread(Socket socket, Handler handler, Context context) {
        Log.e(TAG, "socket:" + socket);
        this.webViewSocket = socket;
        this.handler = handler;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();
        try {
            if (webViewSocket != null) {
                HttpPkg httpPkg = getWebViewRequest();
                if (httpPkg == null) {
                    return;
                }
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
            // 同步界面组件添加
            if (head_line.indexOf("addWebUI") != -1) {
                addWebUi();
            } else if (head_line.indexOf("@@LocalDown") != -1) {// 资源类下载
                downResource(httpPkg);
            } else if (head_line.indexOf("/config/html/") != -1) {// 资源类下载
                getResource(httpPkg);
            } else if (head_line.indexOf("AjAxSocketIFC") != -1) {
                // ajax请求
                handleAjaxReques(head_line);
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

    private void handleAjaxReques(String head_line) throws Exception {
        //需要有返回给webview,否则ajax会等待超时报错
        responseEmptyToWebView();
        if (head_line.indexOf("addWebBtnNum") != -1) {
            addWebBtnNum();
        } else if (head_line.indexOf("@@ShowProgressDialog@@") != -1) {
            showProgressDialog();
        } else if (head_line.indexOf("@@DismissProgressDialog@@") != -1) {
            dismissProgressDialog();
        }
    }

    public static final int ADD_WEB_UI = 2;
    public static final int SHOW_LOADING = 3;
    public static final int DISMISS_LOADING = 4;

    private void responseEmptyToWebView() {
        responseWebView((_CRLF).getBytes(), "".getBytes());
    }

    private void addWebBtnNum() {
    }

    private void showProgressDialog() throws IOException {

        Log.e(TAG, "dismissProgressDialog:" + handler);
        if (handler != null) {
            Message msg = handler.obtainMessage(SHOW_LOADING);
            handler.sendMessage(msg);
        }
    }

    private void dismissProgressDialog() throws IOException {
        Log.e(TAG, "dismissProgressDialog:" + handler);
        if (handler != null) {
            Message msg = handler.obtainMessage(DISMISS_LOADING);
            handler.sendMessage(msg);

        }
    }

    private void addWebUi() throws IOException {
        Log.e(TAG, "addWebUi:" + handler);
        if (handler != null) {
            String jsonStr = getJsonFromWebview();

            Log.v(TAG, "addWebUi:" + jsonStr);
            Message msg = handler.obtainMessage(ADD_WEB_UI);
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
        Log.e(TAG, "filePath:" + filePath.getAbsolutePath());
        try {
            //缓存被删后，重新下载 并写入webview
            if (filePath.exists()) {
                Log.e(TAG, "resource file exist");
                writeCacheToWebView(filename);
            } else {
                Log.e(TAG, "resource file not exist");
                downResource(httpPkg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (filePath != null && filePath.exists()) {
                filePath.delete();
            }
        }
    }

    private void downResource(HttpPkg httpPkg) throws Exception {
        String head_line = httpPkg.getHeadLine();
        String filename = head_line.substring(
                head_line.indexOf("config/html"), head_line.length() - 9);
        File file = new File(DB.RESOURCE_PATH
                + filename);

        Log.v(TAG, "下载资源:" + head_line);

        // 对中间件发送数据包
        Log.v(TAG, "下载资源---请求中间件:");
        ISocketConnection connection = null;
        try {
            /** *2.与中间件交互** */
            connection = SocketFactory.getSSLSocket();

            //到时候Gzip全部去掉
            byte[] content = GzipUtil.byteCompress(httpPkg.getHead().get(HTTP_HEAD).getBytes(), DB.IS_GZIP);

            connection.write(content);


            HashMap<String, String> head_sb = connection.getHttpHead2();
            Log.e(TAG, "head_sb:" + head_sb);
            int contentLength = Integer.valueOf(head_sb.get("Content-Length"));
            byte[] body = connection.getHttpBody(contentLength);

            Log.e(TAG, "body.length:" + body.length);
            responseWebView(head_sb.get("httpHead").getBytes(), body);

            FileUtil.writeFile(file, body);

            Log.e(TAG, "下载完毕,发送通知");
            //通知订阅者下载完一个资源
            EventBus.getDefault().post(new MessageEvent(MessageEvent.RESOURCE_DOWN_SUCC, filename));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
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
//			sb.append("Content-Type: application/octet-stream\r\n".getBytes());
            sb.append(("Content-Length: " + 5 + "\r\n"));// 返回内容字节数
//            sb.append("\r\n");

            responseWebView(sb.toString().getBytes(), "HELLOOOO".getBytes());

//            webView_os.write(sb.toString().getBytes());
//
//            webView_os.write("HELLOOOO".getBytes());
//            ByteArrayOutputStream  byteArrayOutputStream = new ByteArrayOutputStream();
//            int len = 0;
//            security_is = new FileInputStream(cacheFile);
//            while ((j = security_is.read(content)) != -1) {
//                webView_os.write(content, 0, j);
//                len+=j;
//            }
            Log.e(TAG, "cacheFile.length():" + cacheFile.length());
//            Log.e(TAG,"file len:" + len);
//            security_is.close();

//            webView_os.write(byteArrayOutputStream.toByteArray());
//            webView_os.close();
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


    /*****
     * 读取包体信息 返回json字符串 读取流 返回json字符串
     *
     * @return
     * @throws NumberFormatException
     * @throws IOException
     */
    public String getJsonFromWebview() throws NumberFormatException,
            IOException {

        StringBuilder sb = new StringBuilder();
        String jsonData = "";
        int contentLength = 0;
        String line = "";
        while ((line = webView_reader.readLine()) != null) {
            Log.e("request", "line:" + line);
            if ("".equals(line)) {
                break;
            } else if (line.indexOf("Content-Length") != -1) {
                contentLength = Integer.parseInt(line.substring(line
                        .indexOf("Content-Length") + 16));
            }
            sb.append(line + "\r\n");
        }

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        char[] b = new char[1024];
        int i = 0;
        while ((i = webView_reader.read(b, 0, b.length)) > 0) {
//            bos.write(b, 0, i);
            charArrayWriter.write(b, 0, i);
            if (i < 1024)
                break;
        }
//        bos.close();
        charArrayWriter.flush();
        jsonData = charArrayWriter.toString();
        charArrayWriter.close();
        Log.e("request", "getJsonFromWebview:" + contentLength + "---"
                + jsonData.length());
        Log.e("request", "getJsonFromWebview:" + jsonData);
        return jsonData;
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
            Reader reader = new InputStreamReader(webViewSocket.getInputStream());
            webView_reader = new BufferedReader(reader, 1024);
            String firstLine = webView_reader.readLine();
            pkg.setHeadLine(firstLine);
            HashMap<String, String> map = SocketStreamUtil.getHttpHead(webView_reader);
            String head = map.get(HTTP_HEAD);
            if (!TextUtils.isEmpty(head)) {
                map.put(HTTP_HEAD, firstLine + _CRLF + head);
            }
            pkg.setHead(map);
            String contentLength = map.get(Content_Length);
            if (!TextUtils.isEmpty(contentLength)) {
                int len = Integer.valueOf(contentLength);
                byte[] body = SocketStreamUtil.getHttpBody(webViewSocket.getInputStream(), len);
                pkg.setBody(body);
            }
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

        /** *2.与中间件交互** */
        ISocketConnection connection = SocketFactory.getSSLSocket();

        connection.write(GzipUtil.byteCompress(httpPkg.getHead().get(HTTP_HEAD).getBytes(), DB.IS_GZIP));
        connection.write(GzipUtil.byteCompress(httpPkg.getBody(), DB.IS_GZIP));
        Log.e(TAG, "httpRequest 请求数据发起成功:");
        String temp = "";
        try {

            temp = connection.getHttpHead();
//            Log.e(TAG, "httpRequest 返回temp: " + temp);
            if (temp == null)
                throw new IOException();
            if (temp.indexOf("LoginPage") != -1) {
                Log.e(TAG, "注销: ");
                // 页面为 登陆页 超时注销
                Message msg = new Message();
                msg.what = 35;
//                    handler.sendMessage(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        byte[] body = connection.getHttpBody();
        // 3.响应用户请求
//        Log.e(TAG, "body: " + new String(body, "GB18030"));
        responseWebView(temp.getBytes(), body);

        connection.close();
    }

    public void checkConnection() {
        Log.e(TAG, "checkConnection");
        try {
            byte[] head = GzipUtil.byteCompress(("GET /wisp_aas/adapter?open&_method=checkConnection&appcode=" + DB.APP_CODE + _CRLF).getBytes(), DB.IS_GZIP);
            sendRequest(head, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkConnection write over");
    }

    public void versionUpdate() {
        Log.e(TAG, "checkConnection");
        try {
            byte[] head = ("SOCKET /AjAxSocketIFC/versionUpdate/android" + _CRLF + "Content-Length: "
                    + DB.APP_VERSION_ID.length() + _CRLF)
                    .getBytes();
            byte[] body = DB.APP_VERSION_ID
                    .getBytes();

            Log.v(TAG, "更新:" + DB.APP_VERSION_ID);
            Log.v(TAG, "更新:" + new String(head));
            sendRequest(GzipUtil.byteCompress(head, DB.IS_GZIP), GzipUtil.byteCompress(body, DB.IS_GZIP));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkConnection write over");
    }

    private void sendRequest(byte[] head, byte[] body) {
        Log.e(TAG, "sendRequest");
        ISocketConnection connection = SocketFactory.getSSLSocket();

        Log.e(TAG, "sendRequest write begin");
        try {
            connection.write(head);
            connection.write(CRLF);
            if (body != null) {
                connection.write(body);
            }
            Map<String, String> map = connection.getHttpHead2();
            String contentLength = map.get("Content-Length");
            if (!TextUtils.isEmpty(contentLength)) {
                int len = Integer.valueOf(contentLength);
                byte[] content = connection.getHttpBody(len);
                responseWebView(map.get("httpHead").getBytes(), content);
            } else {
                responseWebView(map.get("httpHead").getBytes(), null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "sendRequest write over");
    }

    private void responseWebView(byte[] head, byte[] body) {
        try {
            Log.e(TAG, "socket:" + webViewSocket);
            OutputStream webView_os = webViewSocket.getOutputStream();

//            Log.e("responseWebView", "head123:" + new String(head));

            webView_os.write(head);
            webView_os.write(CRLF);
            if (body != null) {
                webView_os.write(body);
            }
            webView_os.close();
            Log.e("responseWebView", "responseWebView over");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
