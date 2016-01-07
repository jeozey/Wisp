package com.rj.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {
    private static final int BUFFER_LENGTH = 400;
    public static final int BYTE_MIN_LENGTH = 50;


    public static final byte FLAG_GBK_STRING_UNCOMPRESSED_BYTEARRAY = 0;
    public static final byte FLAG_GBK_STRING_COMPRESSED_BYTEARRAY = 1;
    public static final byte FLAG_UTF8_STRING_COMPRESSED_BYTEARRAY = 2;
    public static final byte FLAG_NO_UPDATE_INFO = 3;

    /**
     * 压缩数据
     *
     * @param is
     * @param os
     * @throws Exception
     */
    private static void compress(InputStream is, OutputStream os)
            throws Exception {

        GZIPOutputStream gos = new GZIPOutputStream(os);

        int count;
        byte data[] = new byte[BUFFER_LENGTH];
        while ((count = is.read(data, 0, BUFFER_LENGTH)) != -1) {
            gos.write(data, 0, count);
        }

        gos.finish();

        // gos.flush();
        gos.close();
    }


    /**
     * 解压缩
     *
     * @param is
     * @param os
     * @throws Exception
     */
    private static void decompress(InputStream is, OutputStream os)
            throws Exception {

        GZIPInputStream gis = new GZIPInputStream(is);

        int count;
        byte data[] = new byte[BUFFER_LENGTH];
        while ((count = gis.read(data, 0, BUFFER_LENGTH)) != -1) {
            os.write(data, 0, count);
        }

        gis.close();
    }

    /**
     * 压缩数据
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] byteCompress(byte[] data, boolean flg) throws Exception {
        if (!flg) {
            return data;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        compress(bais, baos);

        byte[] output = baos.toByteArray();

        baos.flush();
        baos.close();

        bais.close();

        return output;
    }

//    /**
//     * gzip 压缩
//     *
//     * @param data
//     * @return
//     * @throws IOException
//     */
//    public static byte[] gzipEncode(byte[] data) throws IOException {
//        ByteArrayOutputStream bosFinal = new ByteArrayOutputStream();
//        GZIPOutputStream gzipOS = new GZIPOutputStream(bosFinal);
//        gzipOS.write(data);
//        gzipOS.flush();
//        gzipOS.close();
//        byte[] wispMsgBodyFinal = bosFinal.toByteArray();
//        bosFinal.close();
//        return wispMsgBodyFinal;
//    }

    /**
     * 解压缩
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] byteDecompress(byte[] data, boolean flg) throws Exception {
        if (!flg) {
            return data;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        decompress(bais, baos);

        data = baos.toByteArray();

        baos.flush();
        baos.close();

        bais.close();

        return data;
    }
}
