package com.rj.view.button;

import java.io.Serializable;
import java.util.List;

public class CustomButton implements Serializable {

    private String beforeimg;
    private String afterimg;
    private String type;
    private String number;
    private String buttontext;
    private String clickevent;
    private String isclick;
    private String parentName;
    private String isNewWind;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    private List<CustomButton> list;

    public List<CustomButton> getList() {
        return list;
    }

    public void setList(List<CustomButton> list) {
        this.list = list;
    }

    public String getBeforeimg() {
        return beforeimg;
    }

    public void setBeforeimg(String beforeimg) {
        this.beforeimg = beforeimg;
    }

    public String getAfterimg() {
        return afterimg;
    }

    public void setAfterimg(String afterimg) {
        this.afterimg = afterimg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getButtontext() {
        return buttontext;
    }

    public void setButtontext(String buttontext) {
        this.buttontext = buttontext;
    }

    public String getClickevent() {
        return clickevent;
    }

    public void setClickevent(String clickevent) {
        this.clickevent = clickevent;
    }

    public String getIsclick() {
        return isclick;
    }

    public void setIsclick(String isclick) {
        this.isclick = isclick;
    }

    public String getIsNewWind() {
        return isNewWind;
    }

    public void setIsNewWind(String isNewWind) {
        this.isNewWind = isNewWind;
    }

    @Override
    public String toString() {
        return "CustomButton [beforeimg=" + beforeimg + ", afterimg="
                + afterimg + ", type=" + type + ", buttontext=" + buttontext
                + ", clickevent=" + clickevent + ", isclick=" + isclick
                + ", parentName=" + parentName + ", isNewWind=" + isNewWind
                + ", list=" + list + "]";
    }

}
