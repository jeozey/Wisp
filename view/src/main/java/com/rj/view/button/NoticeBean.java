package com.rj.view.button;

import java.util.ArrayList;
import java.util.List;

public class NoticeBean {
    private List<String> textList;
    private List<String> textLinkList;
    private String isRoll;
    private String rollType;

    public NoticeBean() {

        textList = new ArrayList<String>();
        textLinkList = new ArrayList<String>();
    }

    public List<String> getTextList() {
        return textList;
    }

    public void setTextList(List<String> textList) {
        this.textList = textList;
    }

    public List<String> getTextLinkList() {
        return textLinkList;
    }

    public void setTextLinkList(List<String> textLinkList) {
        this.textLinkList = textLinkList;
    }

    public String getIsRoll() {
        return isRoll;
    }

    public void setIsRoll(String isRoll) {
        this.isRoll = isRoll;
    }

    public String getRollType() {
        return rollType;
    }

    public void setRollType(String rollType) {
        this.rollType = rollType;
    }

}
