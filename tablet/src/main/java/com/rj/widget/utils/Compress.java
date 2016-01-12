package com.rj.widget.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compress {
	private static final int BUFFER_LENGTH = 400;
	// ????????????????????????????????????????????????????????????????????????????????????????????????
	public static final int BYTE_MIN_LENGTH = 50;

	// ?????????????????????????????????
	public static final byte FLAG_GBK_STRING_UNCOMPRESSED_BYTEARRAY = 0;
	public static final byte FLAG_GBK_STRING_COMPRESSED_BYTEARRAY = 1;
	public static final byte FLAG_UTF8_STRING_COMPRESSED_BYTEARRAY = 2;
	public static final byte FLAG_NO_UPDATE_INFO = 3;

	/**
	 * ????????????
	 *
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void compress(InputStream is, OutputStream os) throws Exception {

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
	 * ???????????????
	 *
	 * @param is
	 * @param os
	 * @throws Exception
	 */
	public static void decompress(InputStream is, OutputStream os) throws Exception {
		GZIPInputStream gis = new GZIPInputStream(is);
		int count;
		byte data[] = new byte[BUFFER_LENGTH];
		while ((count = gis.read(data, 0, BUFFER_LENGTH)) != -1) {
			os.write(data, 0, count);
		}
		gis.close();
	}

	/**
	 * ????????????
	 *
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] byteCompress(byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		compress(bais, baos);
		byte[] output = baos.toByteArray();
		baos.flush();
		baos.close();
		bais.close();
		return output;
	}

	/**
	 * ???????????????
	 *
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] byteDecompress(byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decompress(bais, baos);
		data = baos.toByteArray();
		baos.flush();
		baos.close();
		bais.close();
		return data;
	}

	public static void buildDialog(Context ctx, String title, String message, DialogInterface.OnClickListener confirm, DialogInterface.OnClickListener cancle) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setPositiveButton("确定", confirm);
		dialog.setNegativeButton("取消", cancle);
		dialog.show();
	}


	public static int dp2Px(int dp, Context ctx) {
		return (int) (ctx.getResources().getDisplayMetrics().density * dp + 0.5f);
	}

}
