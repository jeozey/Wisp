package com.rj.wisp.core;

import android.util.Log;

import com.rj.connection.ISocketConnection;
import com.rj.connection.SocketConnectionManager;
import com.rj.connection.SocketConnectionPool;
import com.rj.framework.DB;

/**
 * 作者：志文 on 2015/11/19 0019 15:02
 * 邮箱：594485991@qq.com
 */
public class SocketFactory {
    private static final String TAG = SocketFactory.class.getName();
    private static SocketConnectionPool connectionPool;
    private static boolean isSSL = false;
    public static ISocketConnection getSSLSocket() {
        try {
            Log.e(TAG, "SECURITY_HOST:" + DB.SECURITY_HOST + " SECURITY_PORT:" + DB.SECURITY_PORT);
            if (connectionPool == null) {
                connectionPool = SocketConnectionManager
                        .getInstance().getSocketConnectionPool();
            }

            ISocketConnection connection = null;
            if (!isSSL) {
                connection = connectionPool.getConnection(
                        DB.SECURITY_HOST, DB.SECURITY_PORT, SocketConnectionPool.SOCKET_TYPE.ORIDINARY_SOCKET);
            }
            if (connection == null) {
                connection = connectionPool.getConnection(
                        DB.SECURITY_HOST, DB.SECURITY_PORT, SocketConnectionPool.SOCKET_TYPE.SSL_SOCKET);
                if (connection != null) {
                    isSSL = true;
                }
            }

            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
