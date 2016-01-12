package com.rj.framework.webview;

import android.graphics.Bitmap;
import android.webkit.JsResult;
import android.webkit.WebView;

public interface WebViewCtrol {
    boolean shouldOverrideUrlLoading(WebView view, String url);

    void onPageStarted(WebView view, String url, Bitmap favicon);

    void onPageFinished(WebView view, String url);

    void onCreateWindow(WebView webview);

    void onCloseWindow(WebView webView);

    void onJsAlert(WebView view, String url, String message, JsResult result);

    void onJsConfirm(WebView view, String url, String message,
                     JsResult result);

    void onReceivedError(WebView view, int errorCode, String description, String failingUrl);

    void destoryWebViewCtrol();
}
