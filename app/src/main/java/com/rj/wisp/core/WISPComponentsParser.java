package com.rj.wisp.core;

import com.alibaba.fastjson.JSON;
import com.rj.view.button.ButtonNum;
import com.rj.view.button.CustomButton;
import com.rj.view.button.NoticeBean;
import com.rj.wisp.bean.HandWriting;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WISPComponentsParser {

    public static final String BOTTOM_MENUS_LIST = "bottombtnlist";
    public static final String BOTTOM_MENUS_NUM_LIST = "noticelist";
    public static final String MORE_MENUS_LIST = "collectionlist";
    public static final String TAB_MENUS_LIST = "tabslist";
    public static final String NOTICE_MENUS_LIST = "btnNumlist";

    public static Map<String, List> getCustomButtonList4Json(String data) {
        Map<String, List> map = new HashMap<>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            // 按钮
            JSONArray bntsArray = jsonArray.getJSONArray(0);
            List<CustomButton> list = getCustomButtonList4JsonArray(bntsArray.toString());
            map.put(BOTTOM_MENUS_LIST, list);

            // 按钮集
            JSONArray collectionArray = jsonArray.getJSONArray(1);
            List<CustomButton> list2 = getCustomButtonList4JsonArray(collectionArray.toString());
            map.put(MORE_MENUS_LIST, list2);

            // 页签
            JSONArray tabArray = jsonArray.getJSONArray(2);
            List<CustomButton> list3 = getCustomButtonList4JsonArray(tabArray.toString());
            map.put(TAB_MENUS_LIST, list3);

            // 公告
            JSONArray noticeArray = jsonArray.getJSONArray(3);
            List<NoticeBean> list4 = getNoticeList4JsonArray(noticeArray);
            map.put(NOTICE_MENUS_LIST, list4);

            // 按钮条数
            JSONArray btnNumArray = jsonArray.getJSONArray(4);
            List<ButtonNum> list5 = getButtonNumberList4JsonArray(btnNumArray.toString());
            map.put(BOTTOM_MENUS_NUM_LIST, list5);

            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static List<CustomButton> getCustomButtonList4JsonArray(
            String jsonArray) {
        List<CustomButton> list = new ArrayList<>();
        try {
            list = JSON.parseArray(jsonArray, CustomButton.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<ButtonNum> getButtonNumberList4JsonArray(
            String jsonArray) {
        List<ButtonNum> list = new ArrayList<>();
        try {
            list = JSON.parseArray(jsonArray, ButtonNum.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;

    }

    public static ButtonNum getButtonNumber(String data) {
        ButtonNum buttonNum = new ButtonNum();
        try {
            buttonNum = JSON.parseObject(data, ButtonNum.class);
        } catch (Exception e) {
            return buttonNum;
        }
        return buttonNum;
    }

    public static HandWriting getHandWritingProperty(String jsonStr) {
        HandWriting handWriting = null;
        try {
            handWriting = JSON.parseObject(jsonStr, HandWriting.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handWriting;
    }


    public static List<NoticeBean> getNoticeList4JsonArray(JSONArray jsonArray) {
        List<NoticeBean> list = new ArrayList<NoticeBean>();
        try {

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                NoticeBean noticeBean = new NoticeBean();
                if (!jsonObject.isNull("textList")) {
                    // customButton.setButtontext(jsonObject.getJSONArray("buttonText"));
                    JSONArray textListArray = jsonObject
                            .getJSONArray("textList");
                    // Log.e("wufeng", textListArray.length()+":textListArray");
                    for (int j = 0; j < textListArray.length(); j++) {
                        // JSONObject textList =(JSONObject)
                        // textListArray.get(i);
                        noticeBean.getTextList().add(
                                textListArray.get(j).toString());// Log.e("wufeng",
                        // "text:"+textListArray.get(j).toString());
                    }
                }
                if (!jsonObject.isNull("textLinkList")) {
                    JSONArray clickEventArray = jsonObject
                            .getJSONArray("textLinkList");
                    // Log.e("wufeng",
                    // clickEventArray.length()+":clickEventArray");
                    for (int j = 0; j < clickEventArray.length(); j++) {
                        // JSONObject textList =(JSONObject)
                        // textListArray.get(i);
                        noticeBean.getTextLinkList().add(
                                clickEventArray.get(j).toString());
                        // Log.e("wufeng",
                        // "text:"+clickEventArray.get(j).toString());
                    }
                    // customButton.setClickevent(jsonObject.getJSONArray("clickEvent"));
                }
                if (!jsonObject.isNull("isRoll")) {
                    // customButton.setIsclick(jsonObject.getString("isRoll"));

                }
                if (!jsonObject.isNull("rollType")) {
                    // customButton.setIsclick(jsonObject.getString("rollType"));

                }

                list.add(noticeBean);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
