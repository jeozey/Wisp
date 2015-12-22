package com.rj.wisp.bean;

import java.io.Serializable;

/**
 * 作者：志文 on 2015/12/17 0017 20:10
 * 邮箱：594485991@qq.com
 */
public class ResourceFile implements Serializable {
    private static final String TAG = ResourceFile.class.getName();

    private String filepath = "";
    private String filetype = "";
    private String filemodified = "";
    //下载失败标志,客户端
    private boolean isDownLoadFail = false;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getFilemodified() {
        return filemodified;
    }

    public void setFilemodified(String filemodified) {
        this.filemodified = filemodified;
    }

    public boolean isDownLoadFail() {
        return isDownLoadFail;
    }

    public void setIsDownLoadFail(boolean isDownLoadFail) {
        this.isDownLoadFail = isDownLoadFail;
    }

    @Override
    public String toString() {
        return "filepath:" + filepath + " filetype:" + filetype + " filemodified:" + filemodified + "isDownLoadFail:" + isDownLoadFail;
    }
}
