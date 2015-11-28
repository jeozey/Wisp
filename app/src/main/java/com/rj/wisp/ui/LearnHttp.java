package com.rj.wisp.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LearnHttp {
	private static final byte CR = '\r';
	private static final byte LF = '\n';
	private static final byte[] CRLF = {CR, LF};

	public static void main(String[] args) throws IOException {
		new LearnHttp().testHttp();
	}

	public void testHttp() throws IOException {
		String host = "220.250.1.46";
		Socket socket = new Socket(host, 5599);

		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();

		// 在同一个TCP连接里发送多个HTTP请求
		for (int i = 0; i < 2; i++) {
			writeRequest(out, host);
			readResponse(in);
			System.out.println("\n\n\n");
		}
		out.close();
		in.close();
		socket.close();
	}

	private void writeRequest(OutputStream out, String host) throws IOException {
		// 请求行
		out.write("GET /wisp_aas/config/html/fgwlan/images/720/ico1.png HTTP/1.1".getBytes());
		out.write(CRLF);        // 请求头的每一行都是以CRLF结尾的

		// 请求头
		out.write(("Host: " + host).getBytes()); // 此请求头必须
		out.write(CRLF);

		out.write(CRLF);        // 单独的一行CRLF表示请求头的结束

		// 可选的请求体。GET方法没有请求体

		out.flush();
	}

	private void readResponse(InputStream in) throws IOException {
		// 读取状态行
		String statusLine = readStatusLine(in);
		System.out.println("statusLine :" + statusLine);

		// 消息报头
		Map<String, String> headers = readHeaders(in);

		int contentLength = Integer.valueOf(headers.get("Content-Length"));

		// 可选的响应正文
		byte[] body = readResponseBody(in, contentLength);

		String charset = headers.get("Content-Type");
		if (charset.matches(".+;charset=.+")) {
			charset = charset.split(";")[1].split("=")[1];
		} else {
			charset = "ISO-8859-1";        // 默认编码
		}

		System.out.println("content:\n" + new String(body, charset));
	}

	private byte[] readResponseBody(InputStream in, int contentLength) throws IOException {

		ByteArrayOutputStream buff = new ByteArrayOutputStream(contentLength);

		int b;
		int count = 0;
		while (count++ < contentLength) {
			b = in.read();
			buff.write(b);
		}

		return buff.toByteArray();
	}

	private Map<String, String> readHeaders(InputStream in) throws IOException {
		Map<String, String> headers = new HashMap<String, String>();

		String line;

		StringBuilder sb = new StringBuilder();
		while (!("".equals(line = readLine(in)))) {
			String[] nv = line.split(": ");        // 头部字段的名值都是以(冒号+空格)分隔的
			headers.put(nv[0], nv[1]);
			sb.append(line).append("\r\n");
		}
		sb.append("\r\n");
		System.out.print(sb.toString());
		return headers;
	}

	private String readStatusLine(InputStream in) throws IOException {
		return readLine(in);
	}

	/**
	 * 读取以CRLF分隔的一行，返回结果不包含CRLF
	 */
	private String readLine(InputStream in) throws IOException {
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