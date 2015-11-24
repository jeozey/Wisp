package com.rj.framework.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.rj.framework.DB;

public class WebViewFactory {

    private static int API = android.os.Build.VERSION.SDK_INT;
    private static String mDefaultUserAgent;
    private static WebSettings mSettings;

    @SuppressLint("NewApi")
    public static WebView getNewWebView(Activity activity, String url) {
        Log.e("browser", "getNewWebView()");
        WebView mWebView = new WebView(activity);
        mWebView.setDrawingCacheBackgroundColor(0x00000000);
        mWebView.setFocusableInTouchMode(true);
        mWebView.setFocusable(true);
        mWebView.setAnimationCacheEnabled(false);
        mWebView.setDrawingCacheEnabled(true);
        mWebView.setBackgroundColor(activity.getResources().getColor(
                android.R.color.white));
/*		if (API > 15) {
            mWebView.getRootView().setBackground(null);
		} else {*/
        mWebView.getRootView().setBackgroundDrawable(null);
//		}
        mWebView.setWillNotCacheDrawing(false);
        mWebView.setAlwaysDrawnWithCacheEnabled(true);
        mWebView.setScrollbarFadingEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setSaveEnabled(true);
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mDefaultUserAgent = mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUserAgentString(mDefaultUserAgent + DB.USER_AGENT);
        mWebView.getSettings().setSupportMultipleWindows(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setSaveFormData(false);
        mSettings = mWebView.getSettings();
        initializeSettings(mWebView.getSettings(), activity);

//		Log.e("browser", "url:" + url);
        if (url != null) {
            if (!url.equals("")) {
                mWebView.loadUrl(url);
            }
        } else {
            //load home page
        }
        mWebView.requestFocus();
        return mWebView;
    }

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    public static void initializeSettings(WebSettings settings, Context context) {
        if (API < 18) {
            settings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        if (API < 17) {
            settings.setEnableSmoothTransition(true);
        }
/*		if (API > 16) {
            settings.setMediaPlaybackRequiresUserGesture(true);
		}*/
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
//	public class LightningWebClient extends WebViewClient {
//
//		Context mActivity;
//
//		public LightningWebClient(Context context) {
//			mActivity = context;
//		}
//		
//		// 加载错误时要做的工作
//		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//			view.loadUrl("file:///android_asset/errorPage.html");
//			DB.ERROR_URL = failingUrl;
//		}
//
//		@Override
//		public void onPageFinished(WebView view, String url) {
//		}
//
//		@Override
//		public void onPageStarted(WebView view, String url, Bitmap favicon) {
//		}
//
//
//		@Override
//		public void onScaleChanged(WebView view, float oldScale, float newScale) {
//			if (view.isShown()) {
//				view.invalidate();
//			}
//		}
//
//
//		@Override
//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			Log.v("bug", "rjweb:" + url);
//			if(url.indexOf("_method=attachment") != -1){ //附件下载拦截
//				view.stopLoading(); //下载拦截，停止页面跳转
////				DownLoadDialogTool.allowDownload = true;
//				url = url.replace("_method=attachment", "_method=attachment&client=android");
//				view.loadUrl(url);
//				//更新视图
//// 				DownLoadDialogTool.setContent(activity);
////				DownLoadDialogTool.sendMsg(3,"-1"); //下载对话框
//				return true;
//			} else if (url.contains("tel:") || TextUtils.isDigitsOnly(url)) {
//				mActivity.startActivity(new Intent(Intent.ACTION_DIAL, Uri
//						.parse(url)));
//				return true;
//			} else if (url.startsWith("intent://")) {
//				Intent intent = null;
//				try {
//					intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//				} catch (URISyntaxException ex) {
//					return false;
//				}
//				if (intent != null) {
//					try {
//						mActivity.startActivity(intent);
//					} catch (ActivityNotFoundException e) {
//						Log.e("MyWebView", "ActivityNotFoundException");
//					}
//					return true;
//				}
//			}
//			return super.shouldOverrideUrlLoading(view, url);
//		}
//	}
}
