package com.rj.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public interface ISocketConnection {

    interface DownLoadInvoke {
        void downLoadInvoke(byte[] data, int size);
    }

    InputStream getInputStream();

    OutputStream getOutputStream();

    void write(byte[] data) throws Exception;

    void close();

    String getHttpHead();

    HashMap<String, String> getHttpHead2();

    byte[] getHttpBody();

    byte[] getHttpBody(int len);

    byte[] getHttpBody(int size, DownLoadInvoke downLoadInvoke);

    void shutDownOutPut();
}
