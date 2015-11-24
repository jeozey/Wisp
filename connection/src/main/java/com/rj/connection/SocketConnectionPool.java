package com.rj.connection;

import java.io.IOException;

/**
 * Create at: 2015/11/18 0018 14:17
 *
 * @author：jeo Email：594485991@qq.com
 */
public class SocketConnectionPool {

    public enum SOCKET_TYPE {SDKEY_SOCKET, SSL_SOCKET, ORIDINARY_SOCKET}

    private SocketConnectionFactory connectionFactory;


    public SocketConnectionPool(
            SocketConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setConnectionFactory(SocketConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public ISocketConnection getConnection(String host, int port, SOCKET_TYPE type) throws IOException {

        ISocketConnection newConnection = null;


        if (type == SOCKET_TYPE.SDKEY_SOCKET) {
            newConnection = connectionFactory
                    .createSDKeyConnection(host, port);
        } else if (type == SOCKET_TYPE.ORIDINARY_SOCKET) {
            newConnection = connectionFactory
                    .createOrdinaryConnection(host, port);
        } else {
            try {
                newConnection = connectionFactory
                        .createConnection(SSLServer.getSSLContextInstance(), host, port);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IOException("SSLContext is null,Pls init first!");
            }
        }

        if (newConnection == null) {
            throw new IOException("socket创建失败");
        }
        return newConnection;
    }

}
