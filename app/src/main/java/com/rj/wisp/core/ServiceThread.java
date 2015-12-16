package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.rj.connection.ISocketConnection;
import com.rj.framework.DB;
import com.rj.util.DataUtil;
import com.rj.util.FileUtil;
import com.rj.util.GzipUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：志文 on 2015/11/18 0018 15:19
 * 邮箱：594485991@qq.com
 */
public class ServiceThread extends Thread {
    private static final String TAG = ServiceThread.class.getName();
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
                String head_line = getHeadLine();
                if (head_line == null) {
                    return;
                }
                handleHeadLine(head_line);
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
    private String getHeadLine() {

        try {
            Reader reader = new InputStreamReader(webViewSocket.getInputStream());
            webView_reader = new BufferedReader(reader, 1024);
            webView_os = webViewSocket.getOutputStream();
            String head_line = webView_reader.readLine();
            Log.e("bug", "head_line:" + head_line);
            return head_line;
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e(TAG, "请求第一行出错:" + e1.getMessage());

        }
        return null;
    }

    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte[] CRLF = {CR, LF};

    private void handleHeadLine(String head_line) {
        try {
            // 同步界面组件添加
            if (head_line.indexOf("addWebUI") != -1) {
                addWebUi();
            } else if (head_line.indexOf("/config/html/") != -1) {// 资源类下载
                downLoadResource(head_line);
//                Socket socket = new Socket(DB.SECURITY_HOST, DB.SECURITY_PORT);
//                OutputStream out = socket.getOutputStream();
//                // 请求行
//                out.write("GET /wisp_aas/config/html/fgwlan/images/720/ico1.png HTTP/1.1".getBytes());
//                out.write(CRLF);        // 请求头的每一行都是以CRLF结尾的
//
//                // 请求头
//                out.write(("Host: " + "127.0.0.1:8011").getBytes()); // 此请求头必须
//                out.write(CRLF);
//
//                out.write(CRLF);		// 单独的一行CRLF表示请求头的结束
//
//                // 可选的请求体。GET方法没有请求体
//
//                out.flush();
//
//                readResponse(socket.getInputStream());
//                socket.getOutputStream().close();
//                socket.getInputStream().close();
//                socket.close();
                // ajax请求
            } else if (head_line.indexOf("AjAxSocketIFC") != -1) {
                handleAjaxReques(head_line);
            } else if (head_line.indexOf("LocalSocket") != -1) {
                handleLocalSocketRequest(head_line);
            } else {
                httpRequest(head_line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocalSocketRequest(String head_line) throws Exception {
        if (head_line.indexOf("checkConnection") != -1) {
            checkConnection();
        } else if (head_line.indexOf("versionUpdate") != -1) {
            versionUpdate(head_line);
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
        responseWebView("\r\n\r\n".getBytes(), "".getBytes());
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

    private void downLoadResource(String head_line) throws Exception {
//        head_line = head_line.replace("GET", "HEAD");
        Log.v("bug", "下载资源:" + head_line);
        StringBuilder sb = new StringBuilder();
        sb.append(head_line + "\r\n");

        HashMap<String, String> headMap = SocketStreamUtil.getHttpHead(webView_reader);

        sb.append(headMap.get("httpHead"));
//        sb.append("hi");
        Log.e(TAG, "sb:" + sb);

        String filename = head_line.substring(
                head_line.indexOf("config/html"), head_line.length() - 9);
        File filePath = new File(DB.RESOURCE_PATH
                + filename);

        //缓存被删后，重新下载 并写入webview
        boolean needWriteToWebView = false;
        if (filePath.exists()) {
            writeCacheToWebView(head_line);
            return;
        } else {
            try {
                String filepath = DB.RESOURCE_PATH + filename;
                Log.e(TAG, "filepath0:" + filepath);
//                    if(AjaxGetResourcesTask.sourceMap.containsKey(filepath)){
                Log.e(TAG, "高速缓存被删,重新下载");
                needWriteToWebView = true;
//                    }else{
//                        return;
//                    }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 对中间件发送数据包
        Log.v("bug", "下载资源---请求中间件:");
        ISocketConnection connection = null;
        try {
            /** *2.与中间件交互** */
            connection = SocketFactory.getSSLSocket();

            //到时候Gzip全部去掉
            byte[] body = GzipUtil.byteCompress(sb.toString().getBytes());

            connection.write(body);

//            Map<String,String> head_sb = SocketStreamUtil.readHeaders(socket.getInputStream());

            Log.e(TAG, "filepath:" + filePath);
            // 江志文 保存的是http头，下次直接和文件写到webview
//			FileOutputStream foscache = null;
//			String cachefile = null;
//			cachefile = filepath + "_head";

            HashMap<String, String> head_sb = connection.getHttpHead2();
            Log.e(TAG, "head_sb:" + head_sb);
            int contentLength = Integer.valueOf(head_sb.get("Content-Length"));
//            if (TextUtils.isEmpty(head_sb)) {
//                Log.e("bug", "下载资源出错，0kb");
//            }

//            byte[] content = SocketStreamUtil.readResponseBody(connection.getInputStream(), contentLength);

//			// 写入文件
//            writeResourceFile(filepath, connection.getHttpBody());

            FileUtil.writeFile(filePath.getAbsolutePath(), connection.getHttpBody());
            Log.e(TAG, "写入资源文件成功");

            // 是否是下载新资源，如果下载 成功， 更新本地资源列表
            String path = DB.RESOURCE_PATH
                    + filename;

//            if (!SourceFileUtil.isWriting) {
//                Message msg = Message.obtain();
//                msg.what = 6;
//                msg.obj = new String[] { path, requestfiletype, requestmodified };
//                handler.sendMessage(msg);
//            }

            if (needWriteToWebView) {
                writeCacheToWebView(head_line);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (filePath != null && filePath.exists()) {
                filePath.delete();
            }
            // 异常捕获
            Message msg = Message.obtain();
            msg.what = 7;
            handler.sendMessage(msg);
            e.printStackTrace();
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }


    private void writeCacheToWebView(String head_line)
            throws Exception {
        // 输入
        String requestfilename = head_line.substring(
                head_line.indexOf("config/html"), head_line.length() - 9);
        Log.e("cache", "高速缓存：" + requestfilename);
        byte[] content = new byte[20480];
//		File cacheFileHead = null;
        File cacheFile = null;
        webView_os = webViewSocket.getOutputStream();
        FileInputStream security_is = null;
        DataInputStream security_reader;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int j = 0;


            cacheFile = new File(DB.RESOURCE_PATH
                    + requestfilename);

            byteArrayOutputStream.write("HTTP/1.0 200 OK\r\n".getBytes());//返回应答消息,并结束应答
//			byteArrayOutputStream.write("Content-Type: application/octet-stream\r\n".getBytes());
            byteArrayOutputStream.write(("Content-Length: " + cacheFile.length() + "\r\n").getBytes());// 返回内容字节数
            byteArrayOutputStream.write("\r\n".getBytes());// 根据 HTTP 协议, 空行将结束头信息


            security_is = new FileInputStream(cacheFile);
            while ((j = security_is.read(content, 0, content.length)) != -1) {
                byteArrayOutputStream.write(content, 0, j);
            }
            webView_os.write(byteArrayOutputStream.toByteArray());
            webView_os.flush();
            webView_os.close();
            Log.e(TAG, "缓存写入完成" + cacheFile.getAbsolutePath());
        } catch (FileNotFoundException e0) {
            Log.e(TAG, "资源文件不存在:" + cacheFile.getAbsolutePath());
//			e0.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "第一次缓存写入失败");
            // 打开缓存文件失败。。。。怎么办？ 当然 按原始的方法 去请求
//            writeCacheToWebViewTwice(head_line);
            return;
        } finally {
            if (security_is != null) security_is.close();
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
     * @param head_line
     * @return
     * @throws IOException
     */
    private byte[] getWebViewRequest(String head_line) throws IOException {

        StringBuffer sb = null;
        DataOutputStream os = null;
        boolean isForm = false;
        ByteArrayOutputStream bosFinal = null;
        try {
            bosFinal = new ByteArrayOutputStream();
            os = new DataOutputStream(bosFinal);
            sb = new StringBuffer();
            int contentLength = 0;
            sb.append(head_line + "\r\n");
            if (head_line.indexOf(DB.LOGINPAGE_URL) != -1 && !TextUtils.isEmpty(DB.KEY_SERIAL)) {
                sb.append("KeyInfo: " + DB.KEY_SERIAL + "\r\n");
            }
            String method = head_line.substring(0, 4).trim();
            if ("GET".equalsIgnoreCase(method)) {
                String line2 = "";
                while ((line2 = webView_reader.readLine()) != null) {
                    if ("".equals(line2)) {
                        break;
                    }
                    // 部分手机丢失User-Agent信息 getUserAgentString 江志文 必须找出来哪里丢失的
                    if (line2.contains("User-Agent")
                            && !line2.contains("RJ-WISP-Client")) {
                        line2 += " " + DB.USER_AGENT;
                    }
                    sb.append(line2 + "\r\n");
                }
//				sb.append("filename:adsf \r\n");
                sb.append("\r\n"); // 换行
                // Log.e("DB.RJ_WISP_Client",
                // "DB.RJ_WISP_Client:"+DB.RJ_WISP_Client);
                Log.v("http", sb.toString());
                os.write((sb.toString()).getBytes());
                os.flush();
                os.close();

                /*** look *****/
            } else if ("POST".equalsIgnoreCase(method)) {
                String line2 = "";
                while ((line2 = webView_reader.readLine()) != null) {
                    if ("".equals(line2)) {
                        // sb.append(line2 + "\r\n");
                        break;
                    } else if (line2.indexOf("Content-Length") != -1) {
                        contentLength = Integer.parseInt(line2.substring(line2
                                .indexOf("Content-Length") + 16));
                    } else if (line2.indexOf("multipart/form-data") != -1) {
                        isForm = true;
                    }
                    // 部分手机丢失User-Agent信息 getUserAgentString 江志文 必须找出来哪里丢失的
                    if (line2.contains("User-Agent")
                            && !line2.contains("RJ-WISP-Client")) {
                        line2 += " " + DB.USER_AGENT;
                    }
                    sb.append(line2 + "\r\n");
                }
                if (isForm) {
                    os.write((sb.toString()).getBytes());
                    int postlength = contentLength;// -sb.toString().getBytes().length;
                    char[] buf = new char[1024];
                    int size = 0;
//                    do {
//                        size = webView_reader.read(buf);
//                        os.write(buf, 0, size);
//                        postlength = postlength - size;
//                        if (postlength <= 0) {
//                            break;
//                        }
//                    } while (size != -1);
                    os.flush();

                    os.close();
                } else {
                    byte[] buf = {};
                    int size = 0;
                    if (contentLength != 0) {
                        buf = new byte[contentLength];
                        while (size < contentLength) {
                            int c = webView_reader.read();
                            buf[size++] = (byte) c;
                        }
                        sb.append("\r\n");
                        sb.append(new String(buf, 0, size) + "\r\n");

                    }
                    os.write((sb.toString()).getBytes());

                    os.flush();
                    os.close();
                }
            }
            Log.e("NNN", "sb.toString() = " + sb.toString());
            return bosFinal.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 普通http请求
     *
     * @param head_line
     * @throws Exception
     */

    public void httpRequest(String head_line) throws Exception {

        Log.v("request", "普通:" + head_line);

        /** *1.获取用户请求数据** */
        //这边还得补上keyInfo、userAgent等信息
        HashMap<String, String> webViewRequest = SocketStreamUtil.getHttpHead(webView_reader);


        /** *2.与中间件交互** */
        ISocketConnection connection = SocketFactory.getSSLSocket();

        StringBuilder sb = new StringBuilder();
        sb.append(head_line + "\r\n").append(webViewRequest.get("httpHead"));
        String t = sb.toString().replace("\r", "/");
        String contentLength = webViewRequest.get("Content-Length");
        if (!TextUtils.isEmpty(contentLength)) {
            int len = Integer.valueOf(contentLength);
            byte[] body = SocketStreamUtil.readResponseBody(webView_reader, len);
            Log.e(TAG, "body:" + new String(body));
            sb.append(new String(body));
            Log.e(TAG, "sb:" + sb.toString());
        }
        connection.write(GzipUtil.byteCompress(sb.toString().getBytes()));
        Log.e(TAG, "httpRequest 请求数据发起成功:" + head_line);
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
            byte[] head = GzipUtil.byteCompress(("GET /wisp_aas/adapter?open&_method=checkConnection&appcode=" + DB.APP_CODE + "\r\n").getBytes());
            sendRequest(head, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkConnection write over");
    }

    public void versionUpdate(String head_line) {
        Log.e(TAG, "checkConnection");
        try {
            byte[] head = ("SOCKET /AjAxSocketIFC/versionUpdate/android\r\nUser-Agent: newClient \r\nContent-Length: "
                    + DB.APP_VERSION_ID.length() + "\r\n\r\n")
                    .getBytes();
            byte[] body = DB.APP_VERSION_ID
                    .getBytes();

            Log.v("bug", "更新:" + DB.APP_VERSION_ID);
            Log.v("bug", "更新:" + new String(head));
            sendRequest(DataUtil.gzipEncode(head), DataUtil.gzipEncode(body));
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

            if (head != null) {
                webView_os.write(head);
            }
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
