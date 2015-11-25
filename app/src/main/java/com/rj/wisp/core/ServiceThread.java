package com.rj.wisp.core;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.rj.connection.ISocketConnection;
import com.rj.util.DataUtil;
import com.rj.util.FileUtil;
import com.rj.util.GzipUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

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

    private void handleHeadLine(String head_line) {
        try {
            // 同步界面组件添加
            if (head_line.indexOf("addWebUI") != -1) {
                addWebUi();
            } else if (head_line.indexOf("/config/html/") != -1) {// 资源类下载
                downLoadResource(head_line);
                // 更新条数
            } else if (head_line.indexOf("addWebBtnNum") != -1) {
                addWebBtnNum();
            } else {
                httpRequest(head_line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int ADD_WEB_UI = 2;

    private void responseEmptyToWebView() {
        responseWebView("\r\n\r\n".getBytes(), "".getBytes());
    }

    private void addWebBtnNum() {
        responseEmptyToWebView();
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

    private static File SDFile = new File(DB.SDCARD_PATH
            + "/rjcache/WISPResources/" + DB.SECURITY_HOST + "_"
            + DB.SECURITY_PORT);

    private void downLoadResource(String head_line) throws Exception {
        Log.v("bug", "下载资源:" + head_line);
        String requestfilename = "";// 请求头中带的文件名
        String requestfiletype = "";// 请求头中带的文件类型
        String requestmodified = "";// 请求头中带的文件修改时间
        StringBuilder sb = new StringBuilder();
        String line2 = "";
        sb.append(head_line + "\r\n");


        boolean isDownloadResources = false;// 是否下载资源文件

        while ((line2 = webView_reader.readLine()) != null) {
            if ("Method-Type: download".equals(line2)) {
                isDownloadResources = true;
            }
            if (line2.indexOf("File-Name:") != -1) {
                requestfilename = line2.substring(11, line2.length());
            } else if (line2.indexOf("File-Type:") != -1) {
                requestfiletype = line2.substring(11, line2.length());
            } else if (line2.indexOf("File-Time:") != -1) {
                requestmodified = line2.substring(11, line2.length());
            } else if ("".equals(line2)) {
                sb.append("\r\n");
                break;
            }
            sb.append(line2 + "\r\n");
        }
        // 数据包包头拼装完成
        sb.append("\r\n"); // 换行
//        Log.e(TAG, "sb:"+sb);

        String filename = head_line.substring(
                head_line.indexOf("config/html"), head_line.length() - 9);
        File cacheFile = new File(DB.SDCARD_PATH + "/rjcache/WISPResources/"
                + DB.SECURITY_HOST + "_" + DB.SECURITY_PORT + "/"
                + filename);

        //缓存被删后，重新下载 并写入webview
        boolean needWriteToWebView = false;
        if (!isDownloadResources)// 没有下载资源标示 则 为启用高速缓存
        {
            if (cacheFile.exists()) {
                Log.e(TAG, "启用高速缓存:" + !isDownloadResources);
                writeCacheToWebView(head_line);
                return;
            } else {
                try {
                    String filepath = SDFile.getAbsolutePath() + "/" + filename;
                    Log.e(TAG, "filepath0:" + filepath);
//                    if(AjaxGetResourcesTask.sourceMap.containsKey(filepath)){
                    Log.e(TAG, "高速缓存被删,重新下载");
                    needWriteToWebView = true;
                    requestfilename = filename;
//                    }else{
//                        return;
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        // 对中间件发送数据包
        Log.v("bug", "下载资源---请求中间件:");
        ISocketConnection connection = null;
        try {
            /** *2.与中间件交互** */
            connection = SocketFactory.getSSLSocket();

            byte[] wispMsgBodyFinal = GzipUtil.gzipEncode((sb.toString())
                    .getBytes());
            connection.write(wispMsgBodyFinal);

            if (!SDFile.exists())
                SDFile.mkdirs();

            Log.e("NNN", "requestfilename = " + requestfilename);
            String filepath = SDFile.getAbsolutePath() + "/" + requestfilename;
            File parent = new File(filepath).getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            Log.e(TAG, "filepath:" + filepath);
            // 江志文 保存的是http头，下次直接和文件写到webview
//			FileOutputStream foscache = null;
//			String cachefile = null;
//			cachefile = filepath + "_head";

            String head_sb = connection.getHttpHead();
            if (TextUtils.isEmpty(head_sb)) {
                Log.e("bug", "下载资源出错，0kb");
            }

//			// 写入文件
//            writeResourceFile(filepath, connection.getHttpBody());

            FileUtil.writeFile(filepath, connection.getHttpBody());
            Log.e(TAG, "写入资源文件成功");

            // 是否是下载新资源，如果下载 成功， 更新本地资源列表
            String path = DB.SDCARD_PATH + "/rjcache/WISPResources/"
                    + DB.SECURITY_HOST + "_" + DB.SECURITY_PORT + "/"
                    + requestfilename;

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


            cacheFile = new File(DB.SDCARD_PATH + "/rjcache/WISPResources/"
                    + DB.SECURITY_HOST + "_" + DB.SECURITY_PORT + "/"
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
     * 1.获取用户请求数据
     *
     * @param head_line
     * @return
     * @throws IOException
     */
    private byte[] getWebViewRequest(String head_line) throws IOException {

        StringBuffer sb = null;
        GZIPOutputStream gzipOS = null;
        boolean isForm = false;
        ByteArrayOutputStream bosFinal = null;
        try {
            bosFinal = new ByteArrayOutputStream();
            gzipOS = new GZIPOutputStream(bosFinal);
            sb = new StringBuffer();
            int contentLength = 0;
            sb.append(head_line + "\r\n");
            if (head_line.indexOf(DB.APP_URL) != -1 && !TextUtils.isEmpty(DB.KEY_SERIAL)) {
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
                gzipOS.write((sb.toString()).getBytes());
                gzipOS.flush();
                gzipOS.close();

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
                    gzipOS.write((sb.toString()).getBytes());
                    int postlength = contentLength;// -sb.toString().getBytes().length;
                    char[] buf = new char[1024];
                    StringBuffer stringBuffer = new StringBuffer();
                    int size = 0;
                    do {
                        size = webView_reader.read(buf);
                        stringBuffer.append(new String(buf));
//                        gzipOS.write(buf, 0, size);
                        postlength = postlength - size;
                        if (postlength <= 0) {
                            break;
                        }
                    } while (size != -1);
                    gzipOS.write(stringBuffer.toString().getBytes());
                    gzipOS.flush();

                    gzipOS.close();
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
                    gzipOS.write((sb.toString()).getBytes());

                    gzipOS.flush();
                    gzipOS.close();
                }
            }
//            Log.e("NNN", "sb.toString() = " + sb.toString());
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
        byte[] webViewRequest = getWebViewRequest(head_line);

        /** *2.与中间件交互** */
        ISocketConnection connection = SocketFactory.getSSLSocket();

        // Log.e(TAG, "webViewRequest: " + webViewRequest.length);
        connection.write(webViewRequest);
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
                {
                    handler.sendMessage(msg);
                }
            } else if (temp.indexOf("LoginSuccess") != -1) {
                Log.e(TAG, "LoginSuccess");
                // Message msg = new Message();
                // msg.what = 8;
                // handler.sendMessage(msg);

                Intent intent = new Intent("com.rj.LoginActivity.loginSuccess");
                context.sendBroadcast(intent);

            } else if (temp.indexOf("WISPUserID") != -1) {
                // Editor editor = DB.sp.edit();
                // editor.putString("WISPUserID", temp);
                // editor.commit();
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
        ISocketConnection connection = SocketFactory.getSSLSocket();

        Log.e(TAG, "checkConnection write begin");
        try {
            connection.write(DataUtil.gzipEncode(("GET GET /wisp_aas/adapter?open&_method=checkConnection&appcode=" + DB.APP_CODE + " \r\n\r\n").getBytes()));


            String head = connection.getHttpHead();
            if (head != null) {
                Log.e(TAG, "head1:" + head);
                responseWebView(head.getBytes(), "".getBytes());
            } else {
                Log.e(TAG, "checkConnection failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "checkConnection write over");

    }

    private void responseWebView(byte[] head, byte[] body) {
        try {
            Log.e(TAG, "socket:" + webViewSocket);
            OutputStream webView_os = webViewSocket.getOutputStream();

//            Log.e("responseWebView", "head123:" + new String(head));
            webView_os.write(head);
            webView_os.write(body);
            webView_os.flush();
            webView_os.close();
            Log.e("responseWebView", "responseWebView over");

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }


}
