package com.rj.view.button;

import android.graphics.drawable.Drawable;

import java.util.List;

public class CustomWidgetButton {
    private ButtonType type;// 用于区分部分按钮

    public enum ButtonType {
        LeftBtn
    }

    private Drawable beforeImg;
    private Drawable afterImg;
    private boolean isChooese = false;
    private String title;
    /**
     * 为了方便，当该对象封装全屏/非全屏按钮信息时，无用项num作为标志该次是否为全屏的状态：-1表示非全屏，1表示全屏
     */
    private int num;
    private String callBack;
    private String isclick;
    private List<CustomWidgetButton> popData;// 平板顶部按钮的popwindow子项

    public List<CustomWidgetButton> getPopData() {
        return popData;
    }

    public void setPopData(List<CustomWidgetButton> popData) {
        this.popData = popData;
    }

    public String getCallBack() {
        return callBack;
    }

    public ButtonType getType() {
        return type;
    }

    public void setType(ButtonType type) {
        this.type = type;
    }

    public String getIsclick() {
        return isclick;
    }

    public void setIsclick(String isclick) {
        this.isclick = isclick;
    }

    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }

    public Drawable getBeforeImg() {
        return beforeImg;
    }

    public void setBeforeImg(Drawable beforeImg) {
        this.beforeImg = beforeImg;
    }

    public Drawable getAfterImg() {
        return afterImg;
    }

    public void setAfterImg(Drawable afterImg) {
        this.afterImg = afterImg;
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

    public boolean isChooese() {
        return isChooese;
    }

    public void setChooese(boolean isChooese) {
        this.isChooese = isChooese;
    }

    @Override
    public String toString() {
        return "MyBottomInfo [beforeImg=" + beforeImg + ",afterImg=" + afterImg
                + ", title=" + title + ", num=" + num + ", callBack="
                + callBack + ", isclick=" + isclick + "]";
    }
}
