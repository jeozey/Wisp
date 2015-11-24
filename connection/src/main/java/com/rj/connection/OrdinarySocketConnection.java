package com.rj.connection;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class OrdinarySocketConnection implements ISocketConnection {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;


    private String host = "";
    private int port = 0;

    public OutputStream getOutputStream() {
        return out;
    }

    public OrdinarySocketConnection(String host, int port)
            throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        socket.setSoTimeout(60000); // 超时设置
        // socket.setKeepAlive(true);
        // socket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
        // socket.setSoLinger(true, 0);// socket关闭时，立即释放资源
        // socket.setTrafficClass(0x04 | 0x10);// 高可靠性和最小延迟传输
        socket.setReceiveBufferSize(32 * 1024);
        socket.setSendBufferSize(16 * 1024);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        if (socket == null) {
            throw new IOException("socket创建失败");
        }
    }


    @Override
    public void write(byte[] data) throws Exception {
        if (this.socket == null || !this.socket.isConnected()) {
            socket = new Socket(host, port);
            socket.setSoTimeout(60000); // 超时设置
            // socket.setKeepAlive(true);
            // socket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
            // socket.setSoLinger(true, 0);// socket关闭时，立即释放资源
            // socket.setTrafficClass(0x04 | 0x10);// 高可靠性和最小延迟传输
            socket.setReceiveBufferSize(32 * 1024);
            socket.setSendBufferSize(16 * 1024);
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            if (socket == null) {
                throw new IOException("socket创建失败");
            }
        }

        out.write(data);
        out.flush();
    }


    public String getHttpHead() {
        int tmpChar = 0;
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
                    break;
                }
                if (temp.toString().indexOf("\r\n") > 0) {
                    temp = new StringBuilder("");
                }
            }
//			Log.e("socket", "headline:"+headline);
            return headline.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public HashMap<String, String> getHttpHead2() {
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

    public byte[] getHttpBody() {
        byte[] buffer = new byte[2048];
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len = 0;
            int i = 0;
            while ((len = in.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getHttpBody(int len) {
        byte[] t = new byte[len];
//		Log.e("test7", "contentLen: "+contentLen);
        try {
            in.readFully(t);
            return t;
        } catch (IOException e) {
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
                invoke.downLoadInvoke(b, i);
//					if(len>=size)break;//避免多读，导致connection reset by peer
//					Log.e("test7", "b len: "+len);
//					return b;
//				} else {
//					DownLoadDialogTool.sendMsg(4, "-1"); // 消息发送：取消下载
//					return b;
//				}
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }


    @Override
    public void close() {
        if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                Log.e("socketPool",
                        "destory inputstream error " + e.getMessage());
            }
        }
        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException e) {
                Log.e("socketPool",
                        "outPutStream close error " + e.getMessage());
            }

        }
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                Log.e("socketPool", "destory socket error " + e.getMessage());
            }
        }
        this.out = null;
        this.in = null;
        this.socket = null;
    }
}
