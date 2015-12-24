package com.rj.wisp.core;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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

    private static int getShort(byte[] data) {
        return (data[0] << 8) | data[1] & 0xFF;
    }

    public static HashMap<String, String> getHttpHead(DataInputStream in) {
        try {

            HashMap<String, String> head = new HashMap<String, String>();
            DataInputStream dataInputStream = new DataInputStream(in);
            String temp = "";
            StringBuffer httpHead = new StringBuffer();
            while (!TextUtils.isEmpty(temp = dataInputStream.readLine())) {
                httpHead.append(temp + "\r\n");
                int index = temp.toString().indexOf(":");
                if (index != -1) {
                    String key = temp.toString().substring(0, index);
                    String value = temp.toString().substring(index + 1).replace(" ", "").replace("\r\n", "");
                    head.put(key, value);
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

                if ("".equals(line)) {
                    break;
                }
                httpHead.append(line + "\r\n");

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

    public static byte[] getHttpBody(BufferedReader in, int contentLength) {
        try {
            int len = 0;
            char[] b = new char[1024];//可改成任何需要的值
            StringBuilder sb = new StringBuilder();
            while (len < contentLength) {
                int read = in.read(b, 0, 1024);
                //判断是不是读到了数据流的末尾 ，防止出现死循环。
                if (read == -1 || read < 1024) {
                    break;
                }
                len += read;
                Log.e(TAG, "readBytes:" + len);
                Log.d(TAG, "here:" + new String(b, 0, read));
                sb.append(new String(b, 0, read));
                if (len >= contentLength) {
                    break;
                }

            }
            return sb.toString().getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

        //顶上方法在addWebUI上面有问题，Content-Length和实际的数据长度不一致
//        try {
//            char[] b = new char[1024];
//            int i = 0;
//            StringBuilder sb = new StringBuilder();
//            while ((i = in.read(b, 0, b.length)) > 0) {
////                bos.write(b, 0, i);
//                sb.append(new String(b,0,i));
//                if (i < 1024)
//                    break;
//            }
//            return sb.toString().getBytes();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    public static byte[] getHttpBody(InputStream in, int contentLength) {
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            int b = 0;
//            int i = 0;
//            while ((b = in.read()) != -1) {
//                bos.write(b);
//                i++;
////                Log.e(TAG,"contentLength:"+contentLength+" i:"+i);
//                if (i >= contentLength) {
//                    Log.e(TAG, "contentLength:" + contentLength + " i:" + i + " break");
//                    break;
//                }
//            }
//            return bos.toByteArray();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;

//        try {
//            int readBytes = 0;
//            byte[] b = new byte[contentLength];//可改成任何需要的值
//            int len = b.length;
//            while (readBytes < len) {
//                int read = in.read(b, readBytes, len - readBytes);
//                //判断是不是读到了数据流的末尾 ，防止出现死循环。
//                if (read == -1) {
//                    break;
//                }
//                readBytes += read;
//            }
//            return b;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;

        //顶上方法在addWebUI上面有问题，Content-Length和实际的数据长度不一致
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int size = 1024;
            if (contentLength > 4096) {
                size = 10240;
            } else if (contentLength > 2048) {
                size = 4096;
            } else if (contentLength > 1024) {
                size = 2048;
            }
            byte[] b = new byte[size];
            int i = 0;
            while ((i = in.read(b)) != -1) {
                bos.write(b, 0, i);
//                Log.e(TAG,"i:"+i);
                //这句话不对,不一定都是满的1024,所以不能此处退出
//                if (i < 1024)
//                    break;
                if (i >= contentLength)
                    break;
            }
//            Log.e(TAG,"contentLength:"+contentLength+" bos.toByteArray().length:"+bos.toByteArray().length);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
