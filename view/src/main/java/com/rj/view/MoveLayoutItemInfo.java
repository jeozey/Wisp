package com.rj.view;

import android.graphics.drawable.Drawable;

public class MoveLayoutItemInfo {
    /**
     * 列表项类型:1表示标题栏；其它表示正常项
     */
    private int type;
    private Drawable drawable; // 列表项左边的图片
    private String title; // 列表项名称
    private int num; // 列表项未读数目
    private String callBack; // 列表项回调

    public String getCallBack() {
        return callBack;
    }

    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;

    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "MoveLayoutItemInfo [type=" + type + ", bitmap=" + drawable + ", title=" + title
                + ", num=" + num + ", callBack=" + callBack + "]";
    }

}
