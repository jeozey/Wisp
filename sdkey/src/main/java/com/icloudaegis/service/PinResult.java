package com.icloudaegis.service;

public class PinResult {
    public PinResult(int result, String content) {
        setResult(result);
        setContent(content);
    }

    private int result;
    private String content;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
