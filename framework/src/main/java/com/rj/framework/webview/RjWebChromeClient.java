package com.rj.framework.webview;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class RjWebChromeClient extends WebChromeClient {
    private static final String TAG = "RjWebChromeClient";
    WebViewCtrol webViewCtrol;
    Activity activity;

    public RjWebChromeClient(Activity activity, WebViewCtrol webViewCtrol) {
        this.activity = activity;
        this.webViewCtrol = webViewCtrol;
    }

    @Override
    public void onCloseWindow(WebView webView) {
        Log.e(TAG, "onCloseWindow");
        webViewCtrol.onCloseWindow(webView);
        super.onCloseWindow(webView);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        Log.e(TAG, "onCreateWindow");
        WebView childView = WebViewFactory.getNewWebView(activity, null);
        Log.e(TAG, "USER-AGENT1:" + childView.getSettings().getUserAgentString());
        childView.setWebViewClient(new RjWebViewClient(webViewCtrol));
        childView.setWebChromeClient(new RjWebChromeClient(activity, webViewCtrol));
        webViewCtrol.onCreateWindow(childView, isUserGesture, resultMsg);
        //return isUserGesture; jeozey 20141021导致高版本情况下 A form.submit-->back to A-->window.open(B) B打不开问题
        //isUserGesture	True if the request was initiated by a user gesture, such as the user clicking a link.
        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message,
                             JsResult result) {
        Log.e(TAG, "onJsAlert:" + message);
        webViewCtrol.onJsAlert(view, url, message, result);
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
                               JsResult result) {
        webViewCtrol.onJsConfirm(view, url, message, result);
        return true;
    }

}
