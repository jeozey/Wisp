package com.rj.framework.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsResult;
import android.webkit.WebView;

import com.rj.framework.DB;
import com.rj.framework.ErrorPageUtil;
import com.rj.view.loading.CustomProgressDialog;
import com.rj.view.loading.CutsomProgressDialog;
import com.rj.view.loading.ProgressDialogTool;

public class WebViewCtrolImpl implements WebViewCtrol {
    private static final String TAG = WebViewCtrolImpl.class.getName();
    private Activity activity;
    private AlertDialog jsConfirmDialog, jsAlertDialog;
    private CustomProgressDialog loadDialog = null;
    private CutsomProgressDialog cutsomProgressDialog;
    private static String failingUrl;

    public static String getFailUrl() {
        return failingUrl;
    }

    @Override
    public void destoryWebViewCtrol() {
        dismiss();
        loadDialog = null;
        cutsomProgressDialog = null;
    }

    public WebViewCtrolImpl(Activity activity,
                            CutsomProgressDialog cutsomProgressDialog) {
        this.activity = activity;
        this.cutsomProgressDialog = cutsomProgressDialog;
    }

    public WebViewCtrolImpl(Activity activity) {
        this.activity = activity;
        loadDialog = new ProgressDialogTool().getProgressDialog(activity);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        try {
            currentUrl = url;
            Log.e(TAG, "onPageStarted:" + url);
            if (loadDialog != null && !loadDialog.isShowing()) {
                loadDialog.show();
            }
            if (cutsomProgressDialog != null
                    && !cutsomProgressDialog.isShowing()) {
                cutsomProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String currentUrl;

    private void dismiss() {
        if (loadDialog != null && loadDialog.isShowing())
            loadDialog.dismiss();
        if (cutsomProgressDialog != null && cutsomProgressDialog.isShowing())
            cutsomProgressDialog.dismiss();
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
        Log.v("request", "onPageFinished:" + url);
        dismiss();

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // 拦截tel sms mailto
        if (UrlHandler.startUrl(activity, view, url)) {
            view.stopLoading();
            return true;
        }
        return false;
    }

    @Override
    public void onCreateWindow(WebView view) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCloseWindow(WebView webView) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onJsConfirm(WebView view, String url, String message,
                            final JsResult result) {
        Log.v("bug", "onJsConfirm");
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(DB.APP_NAME).setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        builder.setCancelable(false);
        jsConfirmDialog = builder.create();
        jsConfirmDialog.show();

    }

    @Override
    public void onJsAlert(WebView view, String url, String message,
                          JsResult result) {
        Log.v(TAG, "onJsAlert:" + message);
        try {
            if (activity != null) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(
                        activity);

                builder.setTitle(DB.APP_NAME)
                        .setMessage(message)
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        dialog.cancel();
                                    }
                                });
                builder.setOnKeyListener(new OnKeyListener() {

                    public boolean onKey(DialogInterface dialog, int keyCode,
                                         KeyEvent event) {
                        return true;
                    }
                });
                // 禁止响应按back键的事件
                builder.setCancelable(false);
                jsAlertDialog = builder.create();
                if (activity != null && !activity.isFinishing()) {
                    Log.v("bug", "show");
                    jsAlertDialog.show();
                }
            }
            result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会卡死或变黑显示不了内容。
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.e(TAG, "onReceivedError");
        view.stopLoading();
        view.loadData(ErrorPageUtil.getErrorPage(activity, failingUrl), "text/html", "utf-8");
    }


}
