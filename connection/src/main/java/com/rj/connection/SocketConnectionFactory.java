package com.rj.connection;

import android.util.Log;

import java.io.IOException;

import javax.net.ssl.SSLContext;

/**
 * Create at: 2015/11/17 0017 16:40
 *
 * @author：jeo Email：594485991@qq.com
 */
public class SocketConnectionFactory {
    private static final String TAG = SocketConnectionFactory.class.getName();

    public ISocketConnection createConnection(SSLContext sslContext, String host, int port) {
        ISocketConnection connection = null;
        Log.e(TAG, "host:" + host + " port:" + port);
        connection = new SSLSocketConnection(sslContext, host, port);
        return connection;
    }

    public ISocketConnection createSDKeyConnection(String host, int port) {
        ISocketConnection connection = null;
        try {
            connection = new SDKeySocketConnection(host, port);
        } catch (IOException e) {
            Log.e(TAG, "Create connection error:" + e.getMessage());
        }
        return connection;
    }

    public ISocketConnection createOrdinaryConnection(String host, int port) {
        ISocketConnection connection = null;
        try {
            connection = new OrdinarySocketConnection(host, port);
        } catch (IOException e) {
            Log.e(TAG, "Create connection error:" + e.getMessage());
        }
        return connection;
    }
}
