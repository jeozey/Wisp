package com.rj.view.button;

import java.io.Serializable;
import java.util.List;

public class CustomButton implements Serializable {
    private String beforeImg;
    private String afterImg;
    private String type;
    private String number;
    private String buttonText;
    private String clickEvent;
    private String isClick;
    private String parentName;
    private String isNewWind;

    private List<CustomButton> collction;

    public String getBeforeImg() {
        return beforeImg;
    }

    public void setBeforeImg(String beforeImg) {
        this.beforeImg = beforeImg;
    }

    public String getAfterImg() {
        return afterImg;
    }

    public void setAfterImg(String afterImg) {
        this.afterImg = afterImg;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getClickEvent() {
        return clickEvent;
    }

    public void setClickEvent(String clickEvent) {
        this.clickEvent = clickEvent;
    }

    public String getIsClick() {
        return isClick;
    }

    public void setIsClick(String isClick) {
        this.isClick = isClick;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getIsNewWind() {
        return isNewWind;
    }

    public void setIsNewWind(String isNewWind) {
        this.isNewWind = isNewWind;
    }

    public List<CustomButton> getCollction() {
        return collction;
    }

    public void setCollction(List<CustomButton> collction) {
        this.collction = collction;
    }

    public String toString() {
        return "CustomButton [beforeImg=" + beforeImg + ", afterImg="
                + afterImg + ", type=" + type + ", buttonText=" + buttonText
                + ", clickEvent=" + clickEvent + ", isClick=" + isClick
                + ", parentName=" + parentName + ", isNewWind=" + isNewWind
                + ", collction=" + collction + "]";
    }

}
