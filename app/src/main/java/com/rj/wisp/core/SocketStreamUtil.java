package com.rj.wisp.core;

import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：志文 on 2015/11/26 0026 10:30
 * 邮箱：594485991@qq.com
 */
public class SocketStreamUtil {
    private static final String TAG = SocketStreamUtil.class.getName();

    public static HashMap<String, String> getHttpHead(Socket socket) {
        InputStream in;
        int tmpChar = 0;
        StringBuilder httpHead = new StringBuilder();
        HashMap<String, String> head = new HashMap<String, String>();
        try {
            in = socket.getInputStream();
            StringBuilder temp = new StringBuilder("");
            while ((tmpChar = in.read()) != -1) {
                httpHead.append((char) tmpChar);
                temp.append((char) tmpChar);

//                Log.e("test7", "temp_line:" + temp.toString());
                if (httpHead.toString().indexOf("\r\n\r\n") > 0) {
                    break;
                }
                if (temp.toString().indexOf("\r\n") > 0) {
                    int index = temp.toString().indexOf(":");
                    if (index != -1) {
                        String key = temp.toString().substring(0, index);
                        String value = temp.toString().substring(index + 1).replace(" ", "").replace("\r\n", "");
                        head.put(key, value);
                    }

                    temp = new StringBuilder("");
                }
            }
            head.put("httpHead", httpHead.toString() + "\r\n");
            return head;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, String> getHttpHead(InputStream in) {
        int tmpChar = 0;
        StringBuilder httpHead = new StringBuilder();
        HashMap<String, String> head = new HashMap<String, String>();
        try {
            StringBuilder temp = new StringBuilder("");
            while ((tmpChar = in.read()) != -1) {
                httpHead.append((char) tmpChar);
                temp.append((char) tmpChar);

//                Log.e("test7", "temp_line:" + temp.toString());
                if (httpHead.toString().indexOf("\r\n\r\n") > 0) {
                    break;
                }
                if (temp.toString().indexOf("\r\n") > 0) {
                    int index = temp.toString().indexOf(":");
                    if (index != -1) {
                        String key = temp.toString().substring(0, index);
                        String value = temp.toString().substring(index + 1).replace(" ", "").replace("\r\n", "");
                        head.put(key, value);
                    }

                    temp = new StringBuilder("");
                }
            }
            head.put("httpHead", httpHead.toString() + "\r\n");
            return head;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, String> getHttpHead(BufferedReader bufferedReader) {

        StringBuilder httpHead = new StringBuilder();
        HashMap<String, String> headMap = new HashMap();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
//                Log.e(TAG, "line:" + line);
                httpHead.append(line + "\r\n");

                if ("".equals(line)) {
                    break;
                }
                int index = line.toString().indexOf(":");
                if (index != -1) {
                    String key = line.toString().substring(0, index);
                    String value = line.toString().substring(index + 1).replace(" ", "").replace("\r\n", "");
//                    Log.e(TAG, "key:" + key + " value:" + value);
                    headMap.put(key, value);
                }

            }
            headMap.put("httpHead", httpHead.toString());
//            Log.e(TAG,"headM:"+headMap);
            return headMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /*********************************************/
    private static final byte CR = '\r';
    private static final byte LF = '\n';
    private static final byte[] CRLF = {CR, LF};

    public static Map<String, String> readHeaders(InputStream in) throws IOException {
        Map<String, String> headers = new HashMap<String, String>();

        String line;

        StringBuilder sb = new StringBuilder();
        while (!("".equals(line = readLine(in)))) {
            String[] nv = line.split(": ");        // 头部字段的名值都是以(冒号+空格)分隔的
            if (nv.length == 2) {
                headers.put(nv[0], nv[1]);
            }
            sb.append(line).append("\r\n");
        }
        sb.append("\r\n");
        System.out.print(sb.toString());
        return headers;
    }

    public static byte[] readResponseBody(InputStream in, int contentLength) throws IOException {

        ByteArrayOutputStream buff = new ByteArrayOutputStream(contentLength);

        int b;
        int count = 0;
//        while(count++ < contentLength) {
//            b = in.read();
//            buff.write(b);
//            Log.e(TAG,"count:"+count);
//            System.out.print("count:"+count);
//        }
        byte[] buffer = new byte[10240];
        while ((count = in.read(buffer)) != -1) {
//            Log.e(TAG,"count:"+count);
            buff.write(buffer, 0, count);
        }
        Log.e(TAG, "all count:" + buff.toByteArray().length);
        return buff.toByteArray();
    }

    public static byte[] readResponseBody(BufferedReader in, int contentLength) throws IOException {

        ByteArrayOutputStream buff = new ByteArrayOutputStream(contentLength);

        int b;
        int count = 0;
        while (count++ < contentLength) {
            b = in.read();
            buff.write(b);
//            Log.e(TAG,"count:"+count);
//            System.out.print("count:"+count);
        }
//        byte[] buffer = new byte[10240];
//        while ((count=in.read(buffer))!=-1){
//            Log.e(TAG,"count:"+count);
//            buff.write(buffer,0,count);
//        }
        Log.e(TAG, "all count:" + buff.toByteArray().length);
        return buff.toByteArray();
    }

    public static String readStatusLine(InputStream in) throws IOException {
        return readLine(in);
    }

    /**
     * 读取以CRLF分隔的一行，返回结果不包含CRLF
     */
    public static String readLine(InputStream in) throws IOException {
        int b;

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        while ((b = in.read()) != CR) {
            buff.write(b);
        }

        in.read();        // 读取 LF

        String line = buff.toString();

        return line;
    }
}
