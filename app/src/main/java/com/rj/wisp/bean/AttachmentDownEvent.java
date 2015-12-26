package com.rj.wisp.bean;

/**
 * 作者：志文 on 2015/12/25 0025 16:46
 * 邮箱：594485991@qq.com
 */
public class AttachmentDownEvent {
    private static final String TAG = AttachmentDownEvent.class.getName();


    private String downUrl;
    private String path;
    private String contentType;
    private int hasDownLength;
    private int fileLength;

    private int downResult;
    private String downFailMsg;


    public AttachmentDownEvent(String downUrl, String path, String contentType, int hasDownLength, int fileLength, int downResult) {
        setDownUrl(downUrl);
        setPath(path);
        setContentType(contentType);
        setHasDownLength(hasDownLength);
        setFileLength(fileLength);
        setDownResult(downResult);
    }

    public AttachmentDownEvent(String downUrl, int downResult) {
        setDownUrl(downUrl);
        setDownResult(downResult);
    }

    public AttachmentDownEvent(int downResult) {
        setDownResult(downResult);
    }

    public AttachmentDownEvent(String downFailMsg) {
        setDownFailMsg(downFailMsg);
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getHasDownLength() {
        return hasDownLength;
    }

    public void setHasDownLength(int hasDownLength) {
        this.hasDownLength = hasDownLength;
    }

    public int getFileLength() {
        return fileLength;
    }

    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    public int getDownResult() {
        return downResult;
    }

    public void setDownResult(int downResult) {
        this.downResult = downResult;
    }

    public String getDownFailMsg() {
        return downFailMsg;
    }

    public void setDownFailMsg(String downFailMsg) {
        this.downFailMsg = downFailMsg;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "downUrl:" + downUrl + " path:" + path;
    }
}
