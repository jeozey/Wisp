package com.rj.framework.webview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.rj.view.ToastTool;

public class UrlHandler {
    private static final String TAG = "UrlHandler";

    public enum SEND_TYPE {
        TEL, SMS, EMAIL
    }

    public static String handleError(int errorCode) {
        String errPage = "";
//		if(DB.isPhone){
        errPage = "file:///android_asset/phoneErrorPage.html";
//		}else{
        errPage = "file:///android_asset/errorPage.html";
//		}
        switch (errorCode) {
            case 404:
                errPage = "file:///android_asset/404.html";
                break;
            case 403:
                errPage = "file:///android_asset/403.html";
                break;
            case 502:
                errPage = "file:///android_asset/502.html";
                break;

            default:
                break;
        }
        return errPage;
    }

    public static void handleUrl(Activity activity, SEND_TYPE sendType, String url) {
        if (sendType == SEND_TYPE.TEL) {
            tel(activity, url);
        } else if (sendType == SEND_TYPE.SMS) {
            sms(activity, url);
        } else if (sendType == SEND_TYPE.EMAIL) {
            mail(activity, url);
        }
    }

    private static void tel(Activity activity, String url) {
        Log.e(TAG, "tel url:" + url);
        try {
            Uri uri = Uri.parse("tel:" + url);
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            activity.startActivity(intent);
        } catch (Exception e) {
            ToastTool.show(activity, "很抱歉，请检查号码格式是否正确", Toast.LENGTH_SHORT);

        }
    }

    private static void sms(Activity activity, String url) {
        Log.e(TAG, "sms url:" + url);
        try {
            Uri uri = Uri.parse("sms:" + url);
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            it.putExtra("sms_body", "");
            activity.startActivity(it);
        } catch (Exception e) {
            ToastTool.show(activity, "很抱歉，请检查号码格式是否正确", Toast.LENGTH_SHORT);

        }
    }

    private static void mail(Activity activity, String url) {
        Log.e(TAG, "mail url:" + url);
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            Uri uri = Uri.parse("mailto:" + url);
            intent.setData(uri);
            activity.startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            ToastTool.show(activity, "很抱歉，邮箱启动错误", Toast.LENGTH_SHORT);
        }
    }

    public static Boolean startUrl(Activity activity, WebView view, String url) {
        if (url.indexOf("tel:") != -1) {
            view.stopLoading();
            tel(activity, url);
            return true;
        } else if (url.indexOf("sms:") != -1) {
            view.stopLoading();
            sms(activity, url);
            return true;
        } else if (url.indexOf("mailto:") != -1) {
            view.stopLoading();
            mail(activity, url);
            return true;
        }
        return false;
    }

}
