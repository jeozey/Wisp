package com.rj.framework.download;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadDialogHandler extends Handler {
    DownloadDialog mDownloadDialog;
    public static boolean allowDownload = true;

    public DownloadDialogHandler(DownloadDialog downloadDialog) {
        Log.w("DownloadDialogHandler", "C'tor()");

        mDownloadDialog = downloadDialog;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        DownloadStates downloadStates = (DownloadStates) msg.obj;
//		Log.w("DownloadDialogHandler", "handleMessage():" + downloadStates);

        switch (downloadStates) {
            case MESSAGE_DOWNLOAD_STARTING:
                mDownloadDialog.show();
            case MESSAGE_DOWNLOAD_COMPLETE:
                mDownloadDialog.updateState(downloadStates);
                break;
            case MESSAGE_DOWNLOAD_PROGRESS:
//			Log.w("DownloadDialogHandler", "handleMessage():" + msg.arg1
//					+ "---" + msg.arg2);
                if (msg.arg1 != 0) {
                    mDownloadDialog.updateState(downloadStates, msg.arg2 * 100
                            / msg.arg1);
                }
                break;
            default:
                break;
        }

    }
}