package com.rj.wisp.core;

import android.util.Log;

import com.rj.connection.ISocketConnection;
import com.rj.framework.DB;
import com.rj.socket.pool.SvnOrdinarySocketConnection;

/**
 * 作者：志文 on 2015/11/19 0019 15:02
 * 邮箱：594485991@qq.com
 */
public class SocketFactory {
    private static final String TAG = SocketFactory.class.getName();
    public static ISocketConnection getSSLSocket() {
        try {
            Log.e(TAG, "SECURITY_HOST:" + DB.SECURITY_HOST + " SECURITY_PORT:" + DB.SECURITY_PORT);
            return new SvnOrdinarySocketConnection(DB.SECURITY_HOST, DB.SECURITY_PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
