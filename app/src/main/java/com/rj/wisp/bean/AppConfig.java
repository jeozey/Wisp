package com.rj.wisp.bean;

/**
 * 作者：志文 on 2015/12/15 0015 17:39
 * 邮箱：594485991@qq.com
 */
public class AppConfig {
    private static final String TAG = AppConfig.class.getName();
    private String name;
    private String appcode;
    private String address;
    private String loginpage;
    private String homepage;
    private String charset;
    private int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppcode() {
        return appcode;
    }

    public void setAppcode(String appcode) {
        this.appcode = appcode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLoginpage() {
        return loginpage;
    }

    public void setLoginpage(String loginpage) {
        this.loginpage = loginpage;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
