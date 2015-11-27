package com.rj.wisp.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * 作者：志文 on 2015/11/26 0026 10:30
 * 邮箱：594485991@qq.com
 */
public class SocketStreamUtil {
    private static final String TAG = SocketStreamUtil.class.getName();

    public HashMap<String, String> getHttpHead(Socket socket) {
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

    public static HashMap<String, String> getHttpHead(BufferedReader bufferedReader) {

        StringBuilder httpHead = new StringBuilder();
        HashMap<String, String> headMap = new HashMap();
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
//                Log.e(TAG, "line:" + line);
                httpHead.append(line + "\r\n");

                if ("".equals(line)) {
                    httpHead.append("\r\n");
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
            httpHead.append("\r\n");
            headMap.put("httpHead", httpHead.toString() + "\r\n");
//            Log.e(TAG,"headM:"+headMap);
            return headMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
