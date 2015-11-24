package com.rj.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataUtil {
    public static String getWeekDayString(int i) {
        switch (i) {
            case 0:
                return "Sun";
            case 1:
                return "Mon";

            case 2:
                return "Tus";

            case 3:
                return "Wed";

            case 4:
                return "Thu";

            case 5:
                return "Fri";

            case 6:
                return "Sat";
            default:
                return "Sun";

        }
    }

    /**
     * 获取当前时间
     */
    public static String getCurrentTime() {
        String s = "";
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        s = format.format(date);
        return s;
    }

    /**
     * 解码
     *
     * @param src
     * @return
     */
    public static String unescape(String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(
                            src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(
                            src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(src.substring(lastPos));
                    lastPos = src.length();
                } else {
                    tmp.append(src.substring(lastPos, pos));
                    lastPos = pos;
                }
            }

        }

        return tmp.toString();
    }

    /**
     * 编码
     *
     * @param src
     * @return
     */
    public static String escape(String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);
        for (i = 0; i < src.length(); i++) {
            j = src.charAt(i);
            if (Character.isDigit(j) || Character.isLowerCase(j)
                    || Character.isUpperCase(j))
                tmp.append(j);
            else if (j < 256) {
                tmp.append("%");
                if (j < 16)
                    tmp.append("0");
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    /**
     * 连接字符�?
     *
     * @param buf1
     * @param buf2
     * @return
     */
    public byte[] arraycat(byte[] buf1, byte[] buf2) {
        byte[] bufret = null;
        int len1 = 0;
        int len2 = 0;
        if (buf1 != null)
            len1 = buf1.length;
        if (buf2 != null)
            len2 = buf2.length;
        if (len1 + len2 > 0)
            bufret = new byte[len1 + len2];
        if (len1 > 0)
            System.arraycopy(buf1, 0, bufret, 0, len1);
        if (len2 > 0)
            System.arraycopy(buf2, 0, bufret, len1, len2);
        return bufret;
    }

    /**
     * gzip 压缩
     *
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] gzipEncode(byte[] data) throws IOException {
        ByteArrayOutputStream bosFinal = new ByteArrayOutputStream();
        GZIPOutputStream gzipOS = new GZIPOutputStream(bosFinal);
        gzipOS.write(data);
        gzipOS.flush();
        gzipOS.close();
        byte[] wispMsgBodyFinal = bosFinal.toByteArray();
        bosFinal.close();
        return wispMsgBodyFinal;
    }

    /**
     * gzip 解压缩
     *
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return b;
    }
}
