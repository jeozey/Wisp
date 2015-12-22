package com.rj.wisp.bean;

/**
 * 作者：志文 on 2015/12/21 0021 16:54
 * 邮箱：594485991@qq.com
 */
public class ConnectionStatus {
    private static final String TAG = ConnectionStatus.class.getName();
    //中间件到oa时间
    private long toOaTime;
    //客户端到中间件时间
    private long toServerTime;

    public ConnectionStatus() {
    }

    public ConnectionStatus(long toOaTime, int toServerTime) {
        setToOaTime(toOaTime);
        setToServerTime(toServerTime);
    }

    public long getToOaTime() {
        return toOaTime;
    }

    public void setToOaTime(long toOaTime) {
        this.toOaTime = toOaTime;
    }

    public long getToServerTime() {
        return toServerTime;
    }

    public void setToServerTime(long toServerTime) {
        this.toServerTime = toServerTime;
    }

    @Override
    public String toString() {
        return "toOaTime:" + toOaTime + " toServerTime:" + toServerTime;
    }
}
