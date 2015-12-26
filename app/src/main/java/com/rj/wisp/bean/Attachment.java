package com.rj.wisp.bean;

/**
 * 作者：志文 on 2015/12/26 0026 12:25
 * 邮箱：594485991@qq.com
 */
public class Attachment {
    private static final String TAG = Attachment.class.getName();

    private String url;
    private String path;
    private String contentType;
    private long size;

    public Attachment(String url, String path, String contentType, long size) {
        setUrl(url);
        setPath(path);
        setContentType(contentType);
        setSize(size);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "url:" + url + " path:" + path + " contentType:" + contentType;
    }
}
