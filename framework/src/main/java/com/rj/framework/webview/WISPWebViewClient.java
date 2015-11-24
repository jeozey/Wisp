package com.rj.framework.webview;


import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WISPWebViewClient extends WebViewClient {
    private static final String TAG = "WISPWebViewClient";
    private ProgressDialog loadDialog;
    private Activity activity;
    private int type;

    public WISPWebViewClient(ProgressDialog loadDialog, Activity activity,
                             int type) {
        this.loadDialog = loadDialog;
        this.type = type;
        this.activity = activity;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // Log.e("PhoneStatReceiver", "onPageStart:incomingFlag:" +
        // PhoneStatReceiver.incomingFlag);
        Log.v("request", "onPageStarted:" + url);
//		startUrl(view, url);
        if (type == 0) {
            try {
                if (loadDialog != null && !loadDialog.isShowing()) {
                    loadDialog.show();
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        Log.e(TAG, "onReceivedError:" + failingUrl);
        view.stopLoading();
//		if(DB.isPhone){
//			view.loadUrl("file:///android_asset/phoneErrorPage.html");
//		}else{
        view.loadUrl("file:///android_asset/errorPage.html");
//		}

    }

    @Override
    // 在WebView中而不是默认浏览器中显示页面
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.v("request", "shouldOverrideUrlLoading:" + url);
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {

//		// Log.e("PhoneStatReceiver", "onPageFinish: incomingFlag:" +
//		// PhoneStatReceiver.incomingFlag);
//		Log.v("request", "onPageFinished:" + url);
//		if (!PhoneStatReceiver.incomingFlag) {
//			try {
//				loadDialog.dismiss();
//			} catch (Exception e) {
//			}
//		}
//		PhoneStatReceiver.incomingFlag = false;
    }

    private void ondestroy() {
        // TODO Auto-generated method stub
        if (loadDialog != null) {
            loadDialog.dismiss();
        }

    }

//	private boolean startUrl(WebView view, String url) {
//		if (url.indexOf("_method=attachment") != -1) { // 附件下载拦截
//			view.stopLoading(); // 下载拦截，停止页面跳转
//			if (url.indexOf("@WPSDOCREVISION") != -1) {
//				url = url.substring(0, url.length() - 15);
//				url = url.replace("_method=attachment",
//						"_method=attachment&client=wps");
//				WISPApplication.OpenType = WISPApplication.OPENTYPE_WPS;
//			} else if (url.indexOf("@PDFANNOTATION") != -1) {
//				url = url.substring(0, url.length() - 14);
//				url = url.replace("_method=attachment",
//						"_method=attachment&client=android");
//				WISPApplication.OpenType = WISPApplication.OPENTYPE_ANNOTATION;
//			} else {
//				url = url.replace("_method=attachment",
//						"_method=attachment&client=android");
//				WISPApplication.OpenType = WISPApplication.OPENTYPE_PDF;
//			}
//			DownLoadDialogTool.allowDownload = true;
//			WebView hidden = (WebView) activity.findViewById(view
//					.getResources().getIdentifier("hidden_webview", "id",
//							activity.getPackageName()));
//			hidden.loadUrl(url);
//
//			DownLoadDialogTool.setContent(activity);
//			DownLoadDialogTool.sendMsg(3, "-1"); // 下载对话框
//			return true;
//		} else if (url.indexOf("tel:") != -1) {
//			view.stopLoading();
//			try {
//				Uri uri = Uri.parse(url);
//				Intent intent = new Intent(Intent.ACTION_DIAL, uri);
//				activity.startActivity(intent);
//			} catch (Exception e) {
//				// TODO: handle exception
//				ToastTool.show(activity, "很抱歉，请检查号码格式是否正确", Toast.LENGTH_SHORT);
//
//			}
//
//		} else if (url.indexOf("sms:") != -1) {
//			view.stopLoading();
//			try {
//				Uri uri = Uri.parse(url.replace("sms:", "smsto:"));
//				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
//				it.putExtra("sms_body", "");
//				activity.startActivity(it);
//			} catch (Exception e) {
//				// TODO: handle exception
//				ToastTool.show(activity, "很抱歉，请检查号码格式是否正确", Toast.LENGTH_SHORT);
//
//			}
//
//		} else if (url.indexOf("mailto:") != -1) {
//			try {
//				view.stopLoading();
//				Intent intent = new Intent(Intent.ACTION_SENDTO);
//				Uri uri = Uri.parse(url);
//				intent.setData(uri);
//				activity.startActivity(intent);
//			} catch (Exception e) {
//				// TODO: handle exception
//				ToastTool.show(activity, "很抱歉，邮箱启动错误", Toast.LENGTH_SHORT);
//			}
//		}
//		return false;
//	}
}
