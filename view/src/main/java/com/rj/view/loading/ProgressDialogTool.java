package com.rj.view.loading;

import android.content.Context;

public class ProgressDialogTool {

    private CustomProgressDialog loadDialog = null;

    // :显示转圈圈
    public CustomProgressDialog getProgressDialog(Context context) {
        return getProgressDialog(context, "正在载入中....");
    }

    public CustomProgressDialog getProgressDialog(Context context,
                                                  String message) {
        loadDialog = CustomProgressDialog.createDialog(context);
        loadDialog.setMessage(message);
        // loadDialog = new ProgressDialog(context);
        // loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // loadDialog.setTitle("");
        // loadDialog.setMessage(message);
        // // loadDialog.setProgress(0);
        // loadDialog.setOnDismissListener(new OnDismissListener() {
        // public void onDismiss(DialogInterface dialog) {
        // loadDialog.setProgress(100);
        // }
        // });
        // loadDialog.setOnCancelListener(new OnCancelListener() {
        // public void onCancel(DialogInterface dialog) {
        // loadDialog.setProgress(100);
        // }
        // });
        // loadDialog.setOnShowListener(new OnShowListener() {
        // public void onShow(DialogInterface dialog) {
        // loadDialog.setProgress(0);
        //
        // }
        // });
        // loadDialog.setCanceledOnTouchOutside(false);
        // loadDialog.setMax(100);
        return loadDialog;
    }
}
