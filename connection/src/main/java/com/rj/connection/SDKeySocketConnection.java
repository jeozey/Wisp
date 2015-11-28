package com.rj.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class SDKeySocketConnection implements ISocketConnection {
    public SDKeySocketConnection(String host, int port) throws IOException{
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public void write(byte[] data) throws Exception {

    }


    @Override
    public void close() {

    }

    @Override
    public String getHttpHead() {
        return null;
    }

    @Override
    public HashMap<String, String> getHttpHead2() {
        return null;
    }

    @Override
    public byte[] getHttpBody() {
        return new byte[0];
    }

    @Override
    public byte[] getHttpBody(int len) {
        return new byte[0];
    }

    @Override
    public byte[] getHttpBody(int size, DownLoadInvoke downLoadInvoke) {
        return new byte[0];
    }
}
