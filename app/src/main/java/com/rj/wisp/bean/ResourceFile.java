package com.rj.wisp.bean;

/**
 * 作者：志文 on 2015/12/17 0017 20:10
 * 邮箱：594485991@qq.com
 */
public class ResourceFile {
    private static final String TAG = ResourceFile.class.getName();
    private String filepath = "";
    private String filetype = "";
    private String filemodified = "";

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

    @Override
    public String toString() {
        return "filepath:" + filepath + " filetype:" + filetype + " filemodified:" + filemodified;
    }
}
