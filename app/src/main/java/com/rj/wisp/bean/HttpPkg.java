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

    private int contentLength;
    private String charSet = "utf8";
    private String contentType = "";

    //下载附件进度
    private int contentSize;

    public HttpPkg() {
    }

    public HttpPkg(int contentSize, String charSet, String contentType, int contentLength) {
        setContentSize(contentSize);
        setCharSet(charSet);
        setContentType(contentType);
        setContentLength(contentLength);
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

    public String getCharSet() {
        return charSet;
    }

    public void setCharSet(String charSet) {
        this.charSet = charSet;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentSize() {
        return contentSize;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    @Override
    public String toString() {
        return "firstLine:" + headLine + " contentLength:" + contentLength + " contentType:" + contentType + " charset:" + charSet;
    }
}
