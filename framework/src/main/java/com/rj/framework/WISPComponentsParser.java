package com.rj.framework;

import android.util.Log;

import com.rj.view.button.ButtonNum;
import com.rj.view.button.CustomButton;
import com.rj.view.button.NoticeBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WISPComponentsParser {


    public static List<CustomButton> getTabsList4JsonArray(JSONArray jsonArray) {
        List<CustomButton> list = new ArrayList<CustomButton>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                CustomButton customButton = new CustomButton();
                if (!jsonObject.isNull("buttonText")) {
                    customButton.setButtontext(jsonObject
                            .getString("buttonText"));
                }
                if (!jsonObject.isNull("clickEvent")) {
                    customButton.setClickevent(jsonObject
                            .getString("clickEvent"));
                }
                if (!jsonObject.isNull("isClick")) {
                    customButton.setIsclick(jsonObject.getString("isClick"));

                }
                list.add(customButton);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return list;
        }

        return list;

    }

    public static List<CustomButton> getCustomButtonList4JsonArray(
            JSONArray jsonArray) {
        List<CustomButton> list = new ArrayList<CustomButton>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                CustomButton customButton = new CustomButton();

                if (!jsonObject.isNull("type")) {
                    customButton.setType(jsonObject.getString("type"));
                }
                if (!jsonObject.isNull("number")) {
                    customButton.setNumber(jsonObject.getString("number"));
                }
                if (!jsonObject.isNull("beforeImg")) {
                    customButton
                            .setBeforeimg(jsonObject.getString("beforeImg"));
                }
                if (!jsonObject.isNull("afterImg")) {
                    customButton.setAfterimg(jsonObject.getString("afterImg"));

                }
                if (!jsonObject.isNull("buttonText")) {
                    customButton.setButtontext(jsonObject
                            .getString("buttonText"));

                }
                if (!jsonObject.isNull("clickEvent")) {
                    customButton.setClickevent(jsonObject
                            .getString("clickEvent"));
                }
                if (!jsonObject.isNull("isClick")) {
                    customButton.setIsclick(jsonObject.getString("isClick"));
                }
                if (!jsonObject.isNull("isNewWind")) {
                    customButton
                            .setIsNewWind(jsonObject.getString("isNewWind"));
                }
                if (!jsonObject.isNull("collction")) {
                    customButton
                            .setList(getCustomButtonList4JsonArray(jsonObject
                                    .getJSONArray("collction")));
                }
                list.add(customButton);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return list;
        }

        return list;
    }

    public static List<ButtonNum> getButtonNumberList4JsonArray(
            JSONArray jsonArray) {
        List<ButtonNum> list = new ArrayList<ButtonNum>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                ButtonNum buttonNum = new ButtonNum();

                if (!jsonObject.isNull("type")) {
                    buttonNum.setType(jsonObject.getString("type"));
                }

                if (!jsonObject.isNull("number")) {
                    buttonNum.setNumber(jsonObject.getString("number"));

                }
                if (!jsonObject.isNull("buttonText")) {
                    buttonNum.setButtonText(jsonObject.getString("buttonText"));

                }

                list.add(buttonNum);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return list;
        }

        return list;

    }

    public static ButtonNum getButtonNumber(String data) {
        ButtonNum buttonNum = new ButtonNum();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (!jsonObject.isNull("type")) {
                buttonNum.setType(jsonObject.getString("type"));
            }
            if (!jsonObject.isNull("number")) {
                buttonNum.setNumber(jsonObject.getString("number"));
            }
            if (!jsonObject.isNull("buttonText")) {
                buttonNum.setButtonText(jsonObject.getString("buttonText"));
            }

        } catch (Exception e) {
            // TODO: handle exception
            return buttonNum;
        }
        return buttonNum;
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
            // TODO: handle exception
            e.printStackTrace();
        }
        return list;
    }

    public static Map<String, List> getCustomButtonList4Json(String data) {
        Map<String, List> map = new HashMap<String, List>();
        try {
            // List<CustomButton> list = new ArrayList<CustomButton>();
            JSONArray jsonArray = new JSONArray(data);
            // 按钮
            JSONArray bntsArray = jsonArray.getJSONArray(0);
            List<CustomButton> list = getCustomButtonList4JsonArray(bntsArray);
            map.put("bottombtnlist", list);

            // 按钮集
            JSONArray collectionArray = jsonArray.getJSONArray(1);
            List<CustomButton> list2 = getCustomButtonList4JsonArray(collectionArray);
            map.put("collectionlist", list2);

            // 页签
            JSONArray tabArray = jsonArray.getJSONArray(2);
            List<CustomButton> list3 = getTabsList4JsonArray(tabArray);
            map.put("tabslist", list3);

            // 公告

            JSONArray noticeArray = jsonArray.getJSONArray(3);
            List<NoticeBean> list4 = getNoticeList4JsonArray(noticeArray);
            Log.e("test", "公告:" + noticeArray);
            map.put("noticelist", list4);

            // 按钮条数
            JSONArray btnNumArray = jsonArray.getJSONArray(4);
            List<ButtonNum> list5 = getButtonNumberList4JsonArray(btnNumArray);

            map.put("btnNumlist", list5);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return map;
    }


}
