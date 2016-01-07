package com.rj.socket.pool;

import android.text.TextUtils;
import android.util.Log;

import com.huawei.anyoffice.sdk.socket.SvnSocket;
import com.rj.connection.ISocketConnection;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;


public class SvnOrdinarySocketConnection implements ISocketConnection {
	private String TAG = "OrdinarySocketConnection";
	private SvnSocket svnSocket;

	private InputStream in;
	private OutputStream out;

	private boolean busy = false;
	private String host = "";
	private int port = 0;
	
	@Override
	public InputStream getInputStream() {
		return in;
	}
	@Override
	public OutputStream getOutputStream(){
		return out;
	}

	public SvnOrdinarySocketConnection(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		svnSocket = new SvnSocket(host, port);
		svnSocket.setSoTimeout(60000); // 超时设置
//		 securitySocket.setKeepAlive(true);
//		 securitySocket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
//		 securitySocket.setSoLinger(true, 0);// socket关闭时，立即释放资源
//		 securitySocket.setTrafficClass(0x04 | 0x10);// 高可靠性和最小延迟传输
		svnSocket.setReceiveBufferSize(32 * 1024);
		svnSocket.setSendBufferSize(16 * 1024);

		this.in = new DataInputStream(svnSocket.getInputStream());
		this.out = new DataOutputStream(svnSocket.getOutputStream());
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	@Override
	public void write(byte[] data) throws IOException {
		if (this.svnSocket == null || this.svnSocket.isClosed()) {
			Log.e(TAG, "write init");
			svnSocket = new SvnSocket(host, port);
			svnSocket.setSoTimeout(60000); // 超时设置
			// securitySocket.setKeepAlive(true);
			// securitySocket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
			// securitySocket.setSoLinger(true, 0);// socket关闭时，立即释放资源
			// securitySocket.setTrafficClass(0x04 | 0x10);// 高可靠性和最小延迟传输
			svnSocket.setReceiveBufferSize(32 * 1024);
			svnSocket.setSendBufferSize(16 * 1024);

			this.in = new DataInputStream(svnSocket.getInputStream());
			this.out = new DataOutputStream(svnSocket.getOutputStream());
		}

		Log.e(TAG, "write begin");
		out.write(data);
		out.flush();
		Log.e(TAG, "write over");
	}

//	@Override
//	public byte[] receiveData() throws IOException {
//		byte[] bytes = new byte[1024];
//		int len = in.read(bytes);
//		return bytes;
//	}

	private long contentLen = 0;
	private int getShort(byte[] data) {
        return (int)((data[0]<<8) | data[1]&0xFF);
    }
	public String getHttpHead() {
		try {
			Log.e(TAG, "getHttpHead ");
//			InputStream dis = null;
			BufferedInputStream bis = new BufferedInputStream(in);
	        bis.mark(2);
	        // 取前两个字节
	        byte[] header = new byte[2];
	        int result = bis.read(header);
	        // reset输入流到开始位置
	        bis.reset();
	        // 判断是否是GZIP格式
	        int headerData = getShort(header);
	        // Gzip 流 的前两个字节是 0x1f8b
	        if (result != -1 && headerData == 0x1f8b) {
	            Log.w("HttpTask", "use GZIPInputStream  ");
	            in = new GZIPInputStream(bis);
	        } else {
	        	Log.w("HttpTask", "not use GZIPInputStream");
	        	in = bis;
	        }
	        
	        DataInputStream dataInputStream = new DataInputStream(in);
	        String temp = "";
	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			while(!TextUtils.isEmpty(temp = dataInputStream.readLine())){
				Log.w(TAG, "temp:"+temp);
				if(temp.indexOf("gzip")==-1){
					byteArrayOutputStream.write(temp.getBytes());
					byteArrayOutputStream.write("\r\n".getBytes());
				}
			}
			byteArrayOutputStream.write("\r\n".getBytes());
			return new String(byteArrayOutputStream.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

		
        
		/*int tmpChar = 0;
		StringBuilder headline = new StringBuilder();
		try {
			StringBuilder temp = new StringBuilder("");
			while ((tmpChar = in.read()) != -1) {
				// if(tmpChar==13) Log.e("test7", "13...");
				// if(tmpChar==10) Log.e("test7", "10...");
				headline.append((char) tmpChar);
				temp.append((char) tmpChar);

				// Log.e("test7", "temp_line:"+temp.toString());
				if (headline.toString().indexOf("\r\n\r\n") > 0) {
					Log.e("test7", "headline.toString():" + headline.toString());
					break;
				}
				if (temp.toString().indexOf("\r\n") > 0) {

					if (temp.toString().indexOf("Content-Length") >= 0&&temp.toString().indexOf("WISP-Content-Length") == -1) {
//						Log.e("test7","in......"+temp.toString());
						String tt = temp.toString().replace("\r", "")
								.replace("\n", "");
						contentLen = Long.valueOf(tt.substring(tt
								.indexOf(": ") + 2));
						// bodyLen=Long.parseLong(temp.toString().substring(temp.toString().indexOf(": ")+2),temp.toString().indexOf("\r"));
						Log.e("test7", "my-content-length:" + contentLen);
					}
					temp = new StringBuilder("");
				}
			}
//			Log.e("socket", "headline:"+headline);
			return headline.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;*/

	}
	
	@Override
	public HashMap<String, String> getHttpHead2() {
		try {
//			InputStream dis = null;
			BufferedInputStream bis = new BufferedInputStream(in);
	        bis.mark(2);
	        // 取前两个字节
	        byte[] header = new byte[2];
	        int result = bis.read(header);
	        // reset输入流到开始位置
	        bis.reset();
	        // 判断是否是GZIP格式
	        int headerData = getShort(header);
	        // Gzip 流 的前两个字节是 0x1f8b
	        if (result != -1 && headerData == 0x1f8b) {
	            Log.w("HttpTask", "use GZIPInputStream  ");
	            in = new GZIPInputStream(bis);
	        } else {
	        	Log.w("HttpTask", "not use GZIPInputStream");
	        	in = bis;
	        }
	        
	        HashMap<String, String> head = new HashMap<String, String>();
	        DataInputStream dataInputStream = new DataInputStream(in);
	        String temp = "";
	        StringBuffer httpHead = new StringBuffer();
			while(!TextUtils.isEmpty(temp = dataInputStream.readLine())){
				httpHead.append(temp+"\r\n");
				Log.w(TAG, "temp:"+temp);
				if(temp.indexOf("gzip")==-1){
					int index = temp.toString().indexOf(":");
					if(index!=-1){
						String key = temp.toString().substring(0,index);
						String value = temp.toString().substring(index+1).replace(" ", "").replace("\r\n", "");
						head.put(key, value);
					}
				}
			}
			if(head.get("Content-Length")!=null){
				try {
					contentLen = Long.valueOf(head.get("Content-Length"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			head.put("httpHead", httpHead.toString() + "\r\n");
			return head;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
		
		/*int tmpChar = 0;
		StringBuilder headline = new StringBuilder();
		HashMap<String, String> head = new HashMap<String, String>();
		try {
			StringBuilder temp = new StringBuilder("");
			while ((tmpChar = in.read()) != -1) {
				headline.append((char) tmpChar);
				temp.append((char) tmpChar);

				// Log.e("test7", "temp_line:"+temp.toString());
				if (headline.toString().indexOf("\r\n\r\n") > 0) {
					break;
				}
				if (temp.toString().indexOf("\r\n") > 0) {
					int index = temp.toString().indexOf(":");
					if(index!=-1){
						String key = temp.toString().substring(0,index);
						String value = temp.toString().substring(index+1).replace(" ", "").replace("\r\n", "");
						head.put(key, value);
					}
					
					temp = new StringBuilder("");
				}
			}
			contentLen = Long.valueOf(head.get("Content-Length"));
			Log.e("NNN", "headline = " + headline);
			return head;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;*/
	}
	
	public byte[] getHttpBody(){
		/*byte[] t = new byte[(int)contentLen];
//		Log.e("test7", "contentLen: "+contentLen);
		try {
			in.readFully(t);
			return t;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;*/
		
		byte[] buffer = new byte[2048];
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			int len = 0;
			int i=0;
			while ((len = in.read(buffer)) != -1) {
				byteArrayOutputStream.write(buffer, 0, len);
//				Log.e(TAG, "len:"+i+++"--"+len);
//				if(len<2048)break;//避免多读，导致connection reset by peer
			}
			return byteArrayOutputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public byte[] getHttpBody(int len) {
		Log.e("socket", "len: " + len);
		try {
			int i = 0;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1];
			while (i<len&&in.read(b, 0, 1)!=-1) {
				bos.write(b, 0, 1);
				i++;
			}
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public byte[] getHttpBody(int size, DownLoadInvoke invoke) {
		byte[] b = new byte[size]; // 10kb
		int i = 0;
		int len = 0;
		try {
			while ((i = in.read(b, 0, b.length)) != -1) { // exception:
//				if (DownLoadDialogTool.allowDownload) {
					len += i;
					invoke.downLoadInvoke(b,i);
//					if(len>=size)break;//避免多读，导致connection reset by peer
//					Log.e("test7", "b len: "+len);
//					return b;
//				} else {
//					DownLoadDialogTool.sendMsg(4, "-1"); // 消息发送：取消下载
//					return b;
//				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}

	public void close() {
		this.busy = false;
	}


	@Override
	public void shutDownOutPut() {
		try {
			if (svnSocket != null) {
				svnSocket.shutdownOutput();
			}
		} catch (Exception e) {

		}

	}
}
