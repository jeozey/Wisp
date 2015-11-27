package com.rj.wisp.core;

import com.rj.connection.ISocketConnection;
import com.rj.connection.SocketConnectionManager;
import com.rj.connection.SocketConnectionPool;
import com.rj.framework.DB;

/**
 * 作者：志文 on 2015/11/19 0019 15:02
 * 邮箱：594485991@qq.com
 */
public class SocketFactory {
    private static SocketConnectionPool connectionPool;

    public static ISocketConnection getSSLSocket() {
        try {
            if (connectionPool == null) {
                connectionPool = SocketConnectionManager
                        .getInstance().getSocketConnectionPool();
            }
            ISocketConnection connection = connectionPool.getConnection(
                    DB.SECURITY_HOST, DB.SECURITY_PORT, SocketConnectionPool.SOCKET_TYPE.ORIDINARY_SOCKET);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
