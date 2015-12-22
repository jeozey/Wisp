package com.rj.connection;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

/**
 * Create at: 2015/11/17 0017 16:42
 *
 * @author：jeo Email：594485991@qq.com
 */
public class SSLSocketConnection implements ISocketConnection {
    private String TAG = SSLSocketConnection.class.getName();
    private Socket sslSocket;

    private InputStream in;
    private OutputStream out;

    private SSLContext sslContext;
    private String host = "";
    private int port = 0;

    private void initInOutStream() {
        getInputStream();
        getOutputStream();
    }
    public OutputStream getOutputStream() {
        try {
            if (out == null) out = new DataOutputStream(sslSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public InputStream getInputStream() {
        try {
            if (in == null) in = new DataInputStream(sslSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    private SSLSocketConnection() {
    }

    public SSLSocketConnection(SSLContext sslContext, String host, int port) {
//        Log.e(TAG, "init 0");
        this.sslContext = sslContext;
        this.host = host;
        this.port = port;
        sslSocket = initSSLSocket(sslContext, host, port);
//        Log.e(TAG, "init 2");


//        Log.e(TAG, "init 3");
    }

    private SSLSocket initSSLSocket(SSLContext sslContext, String host, int port) {
        try {
//            Log.e(TAG, "create socket 1");
//            Socket proxy = new Socket(host,
//                    port);
//            Log.e(TAG,"create socket 2");
//            SSLSocket sslSocket = (SSLSocket) sslContext
//                    .getSocketFactory().createSocket(proxy,
//                            host, port,
//                            true);

            //为什么这种方法创建sslsocket更快
            SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory()
                    .createSocket(host, port);
//            Log.e(TAG, "create socket 3");

//		securitySocket = (SSLSocket) sslContext.getSocketFactory()
//				.createSocket(host, port);
            sslSocket.setSoTimeout(60000); // 超时设置
            sslSocket.setKeepAlive(true);
            sslSocket.setTcpNoDelay(true);// 数据不作缓冲，立即发送
            sslSocket.setSoLinger(true, 0);// socket关闭时，立即释放资源
            sslSocket.setTrafficClass(0x04 | 0x10);// 高可靠性和最小延迟传输
            sslSocket.setReceiveBufferSize(32 * 1024);
            sslSocket.setSendBufferSize(16 * 1024);

            return sslSocket;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void write(byte[] data) throws IOException {
        if (out == null) {
            initInOutStream();
        }

        if (data != null) {
            Log.e(TAG, "write begin");
            out.write(data);
            out.flush();
            Log.e(TAG, "write over");
        }
    }


    private int getShort(byte[] data) {
        return (data[0] << 8) | data[1] & 0xFF;
    }

    public String getHttpHead() {
        try {
            if (in == null) {
                initInOutStream();
            }
            Log.e(TAG, "getHttpHead ");
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
                Log.w(TAG, "use GZIPInputStream  ");
                in = new GZIPInputStream(bis);
            } else {
                Log.w(TAG, "not use GZIPInputStream");
                in = bis;
            }

            DataInputStream dataInputStream = new DataInputStream(in);
            String temp = "";
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (!TextUtils.isEmpty(temp = dataInputStream.readLine())) {
                if (temp.indexOf("gzip") == -1) {
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


    }

    @Override
    public HashMap<String, String> getHttpHead2() {
        try {
            if (in == null) {
                initInOutStream();
            }
//            BufferedInputStream bis = new BufferedInputStream(in);
//            bis.mark(2);
//            // 取前两个字节
//            byte[] header = new byte[2];
//            int result = bis.read(header);
//            // reset输入流到开始位置
//            bis.reset();
//            // 判断是否是GZIP格式
//            int headerData = getShort(header);
//            // Gzip 流 的前两个字节是 0x1f8b
//            if (result != -1 && headerData == 0x1f8b) {
//                Log.w(TAG, "use GZIPInputStream  ");
//                in = new GZIPInputStream(bis);
//            } else {
//                Log.w(TAG, "not use GZIPInputStream");
//                in = bis;
//            }

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
            head.put("httpHead", httpHead.toString());
//            head.put("httpHead", httpHead.toString() + "\r\n");
            return head;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;


    }

    public byte[] getHttpBody() {
        if (in == null) {
            initInOutStream();
        }
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
        Log.e("socket", "len: " + len);
        if (in == null) {
            initInOutStream();
        }
        try {
            int i = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int buffer = 0;
            while (i < len && (buffer = in.read()) != -1) {
                bos.write(buffer);
                i++;
                if (i >= len) {
                    break;
                }
            }
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getHttpBody(int size, DownLoadInvoke invoke) {
        if (in == null) {
            initInOutStream();
        }
        byte[] b = new byte[size]; // 10kb
        int i = 0;
        int len = 0;
        try {
            while ((i = in.read(b, 0, b.length)) != -1) { // exception:
                len += i;
                invoke.downLoadInvoke(b, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }


    @Override
    public void close() {
        /*if (this.in != null) {
            try {
                this.in.close();
            } catch (IOException e) {
                Log.e("socketPool",
                        "inputstream  close error " + e.getMessage());
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
        if (this.sslSocket != null) {
            try {
                this.sslSocket.close();
            } catch (IOException e) {
                Log.e("socketPool", "securitySocket close error " + e.getMessage());
            }
        }
        this.out = null;
        this.in = null;
        this.sslSocket = null;*/
    }

    @Override
    public void shutDownOutPut() {
        try {
            if (sslSocket != null) {
                sslSocket.shutdownOutput();
            }
        } catch (Exception e) {

        }

    }
}
