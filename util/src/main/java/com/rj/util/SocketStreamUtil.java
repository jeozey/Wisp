package com.rj.util;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SocketStreamUtil {
    public static HashMap<String, String> getHttpResponse(InputStream in) {
        int tmpChar = 0;
        int contentLength = 0;
        StringBuilder headline = new StringBuilder();
        HashMap<String, String> response = new HashMap<String, String>();
        try {
            StringBuilder temp = new StringBuilder("");
            StringBuffer httpHead = new StringBuffer();
            while ((tmpChar = in.read()) != -1) {
                headline.append((char) tmpChar);
                temp.append((char) tmpChar);

                // Log.e("test7", "temp_line:"+temp.toString());
                if (headline.toString().indexOf("\r\n\r\n") > 0) {
                    Log.i("GuLang", "读头{" + response + "}");
                    contentLength = 0;
                    if (response.get("Content-Length") != null) {
                        try {
                            contentLength = Integer.valueOf(response.get("Content-Length"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            contentLength = 0;
                        }
                    }
                    if (contentLength >= 0) {
                        int len;
                        int all = 0;
                        byte[] b = new byte[8];
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        Log.i("GuLang", "进入读体循环");
                        while ((len = in.read(b)) != -1) {
                            bos.write(b, 0, len);
                            Log.i("GuLang", "读体循环");
                            all += len;
                            if (all >= contentLength)
                                break;
                        }
                        Log.i("GuLang", "循环结束");
                        // while ((len = in.read(b, 0, 1)) > 0) {
                        // bos.write(b, 0, len);
                        // Log.i("CheckNewVersionTool", "读体循环");
                        // String result = bos.toString("utf-8");
                        // Log.v("bug", "包体1:" + result);
                        // all += len;
                        // if (all >= contentLength)
                        // break;
                        // }
                        response.put("Body", bos.toString("utf-8"));
                    }
                    break;
                }
                if (temp.toString().indexOf("\r\n") > 0) {
                    int index = temp.toString().indexOf(":");
                    if (index != -1) {
                        String key = temp.toString().substring(0, index);
                        String value = temp.toString().substring(index + 1).replace(" ", "")
                                .replace("\r\n", "");
                        response.put(key, value);
                    }
                    httpHead.append(temp.toString());
                    temp = new StringBuilder("");
                }
            }

            response.put("httpHead", httpHead.toString() + "\r\n");
            Log.e("NNN", "headline = " + headline);
            return response;
        } catch (Exception e) {
            Log.e("CheckNewVersionTool", "工具方法抛出异常");
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, String> getHttpHead2(InputStream in) {
        int tmpChar = 0;
        StringBuilder headline = new StringBuilder();
        HashMap<String, String> head = new HashMap<String, String>();
        try {
            StringBuilder temp = new StringBuilder("");
            StringBuffer httpHead = new StringBuffer();
            while ((tmpChar = in.read()) != -1) {
                headline.append((char) tmpChar);
                temp.append((char) tmpChar);

                // Log.e("test7", "temp_line:"+temp.toString());
                if (headline.toString().indexOf("\r\n\r\n") > 0) {
                    break;
                }
                if (temp.toString().indexOf("\r\n") > 0) {
                    int index = temp.toString().indexOf(":");
                    if (index != -1) {
                        String key = temp.toString().substring(0, index);
                        String value = temp.toString().substring(index + 1).replace(" ", "")
                                .replace("\r\n", "");
                        head.put(key, value);
                    }
                    httpHead.append(temp.toString());
                    temp = new StringBuilder("");
                }
            }

            head.put("httpHead", httpHead.toString() + "\r\n");
            Log.e("NNN", "headline = " + headline);
            return head;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
