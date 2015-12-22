package com.rj.wisp.bean;

import java.util.HashMap;

/**
 * 作者：志文 on 2015/12/20 0020 20:20
 * 邮箱：594485991@qq.com
 */
public class HttpPkg {
    private static final String TAG = HttpPkg.class.getName();

    private String headLine;
    private HashMap<String, String> head;
    private byte[] body;

    public HttpPkg() {
    }

    public HttpPkg(HashMap<String, String> head) {
        setHead(head);
    }

    public HttpPkg(HashMap<String, String> head, byte[] body) {
        setHead(head);
        setBody(body);
    }

    public String getHeadLine() {
        return headLine;
    }

    public void setHeadLine(String headLine) {
        this.headLine = headLine;
    }

    public HashMap<String, String> getHead() {
        return head;
    }

    public void setHead(HashMap<String, String> head) {
        this.head = head;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
