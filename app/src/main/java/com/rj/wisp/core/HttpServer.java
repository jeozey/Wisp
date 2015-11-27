package com.rj.wisp.core;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.rj.framework.DB;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
    private static final String TAG = HttpServer.class.getName();
    private static ServerSocket serverSocket = null;
    private Handler handler = null;
    private Context context;
    private static Boolean hasStart = false;
    private static Boolean stopFlg = false;

    public static Boolean getHasStart() {
        return hasStart;
    }

    public static void stopHttpServer() {
        Log.e(TAG, "stopHttpServer0");
        hasStart = false;
        stopFlg = true;

        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "stopHttpServer1");
    }

    public void changeHttpServer(Handler handler, Context context) {
        this.handler = handler;
        this.context = context;
    }

    public HttpServer(Handler handler, Context context) throws Exception {
        try {
            if (hasStart) {
                Log.e(TAG, "HttpServer has started");
                return;
            }
            this.handler = handler;
            this.context = context;
            Log.e(TAG, "开启服务线程");
            if (serverSocket == null) {
//				serverSocket = new ServerSocket();
//				int size = serverSocket.getReceiveBufferSize();
//				if (size < 131072)
//					serverSocket.setReceiveBufferSize(131072); // 把缓冲区的大小设为128K
//				Log.e(TAG, "bind:"+DB.HTTPSERVER_HOST+":"+DB.HTTPSERVER_PORT);
//				serverSocket.bind(new InetSocketAddress(DB.HTTPSERVER_PORT)); // 与端口绑定
//				serverSocket.setSoTimeout(30000); // 超时设置
                serverSocket = new ServerSocket(DB.HTTPSERVER_PORT);
            }
            // 启动线程
            Thread thread = new Thread(this);
            thread.setDaemon(true);// 守护线程
            thread.start();
            Log.e(TAG, "启动HttpServer成功！端口为：" + DB.HTTPSERVER_PORT);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void run() {
        try {
            Socket webViewSocket = null;

            hasStart = true;
            stopFlg = false;
            while (!stopFlg) {
                try {
                    webViewSocket = serverSocket.accept();
                    webViewSocket.setSoTimeout(60000); // 超时设置
//					webViewSocket.setKeepAlive(true);

                    new Thread(new ServiceThread(webViewSocket, handler, context)).start();

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (webViewSocket != null) {
                            webViewSocket.close();
                            webViewSocket = null;
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            Log.e(TAG, "stop");
            hasStart = false;
            stopFlg = true;

            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 启动HTTP服务
//	public static void startServer(final Handler handler) throws Exception {
//		if (!hasStart) {
//			new HttpServer(handler);
//		}
//	}
}
