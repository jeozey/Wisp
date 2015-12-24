package com.rj.framework.webview;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.rj.framework.DB;

public class WebViewFactory {

    private static int API = android.os.Build.VERSION.SDK_INT;

    public static WebView getNewWebView(Context context, String url) {
        Log.e("browser", "getNewWebView()");
        WebView mWebView = new WebView(context);
        initWebView(mWebView);

        initWebView(context, mWebView);

        if (!TextUtils.isEmpty(url)) {
            mWebView.loadUrl(url);
        }

        mWebView.requestFocus();
        return mWebView;
    }

    private static void initWebView(WebView webView) {
        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            webView.getSettings().setLoadsImagesAutomatically(false);
        }
    }

    public static void initWebView(Context context, WebView mWebView) {
        if (mWebView != null) {
            mWebView.setDrawingCacheBackgroundColor(0x00000000);
            mWebView.setFocusableInTouchMode(true);
            mWebView.setFocusable(true);
            mWebView.setAnimationCacheEnabled(false);
            mWebView.setDrawingCacheEnabled(true);
            mWebView.setBackgroundColor(context.getResources().getColor(
                    android.R.color.white));
            mWebView.getRootView().setBackgroundDrawable(null);
            mWebView.setWillNotCacheDrawing(false);
            mWebView.setAlwaysDrawnWithCacheEnabled(true);
            mWebView.setScrollbarFadingEnabled(true);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.setVerticalScrollBarEnabled(true);
            mWebView.setSaveEnabled(true);
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            initializeSettings(mWebView.getSettings(), context);
        }
    }

    public static void initializeSettings(WebSettings settings, Context context) {
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setUserAgentString(settings.getUserAgentString() + DB.USER_AGENT);
        settings.setSupportMultipleWindows(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);//设置为true,系统会弹出AlertDialog确认框
        if (API < 18) {
            settings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        if (API < 17) {
            settings.setEnableSmoothTransition(true);
        }
        if (API < 19) {
            settings.setDatabasePath(context.getFilesDir().getAbsolutePath()
                    + "/databases");
        }
        settings.setDomStorageEnabled(true);
        settings.setAppCachePath(context.getCacheDir().toString());
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setGeolocationDatabasePath(context.getCacheDir()
                .getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setDefaultTextEncodingName("utf-8");
		/*if (API > 16) {
			settings.setAllowFileAccessFromFileURLs(false);
			settings.setAllowUniversalAccessFromFileURLs(false);
		}*/
    }
}
