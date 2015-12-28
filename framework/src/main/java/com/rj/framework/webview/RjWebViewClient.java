package com.rj.framework.webview;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class RjWebViewClient extends WebViewClient {
    private static final String TAG = "RjWebViewClient";
    WebViewCtrol webViewCtrol;


    public RjWebViewClient(WebViewCtrol webViewCtrol) {
        this.webViewCtrol = webViewCtrol;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.e(TAG, "shouldOverrideUrlLoading:" + url);
        return webViewCtrol.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
//        Log.e(TAG, "onPageStarted:" + url);
        webViewCtrol.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.e(TAG, "onPageFinished:" + url);
        webViewCtrol.onPageFinished(view, url);
        if (!view.getSettings().getLoadsImagesAutomatically()) {
            view.getSettings().setLoadsImagesAutomatically(true);
        }
    }


//    @Override
//    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//        super.onReceivedError(view, errorCode, description, failingUrl);
//        webViewCtrol.onReceivedError(view, errorCode, description, failingUrl);
//    }

}
