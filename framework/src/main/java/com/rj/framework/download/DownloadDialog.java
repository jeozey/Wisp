package com.rj.framework.download;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;

import com.rj.framework.R;


public class DownloadDialog {
    private static final String TAG = "DownloadDialog";
    private ProgressDialog mProgressDialog = null;
    private Context mContext = null;
    private Resources mResources = null;
    private CancleDownLoad cancleDownLoad;

    public interface CancleDownLoad {
        void cancle();
    }

    public DownloadDialog(Context context, CancleDownLoad cancleDownLoad) {
        Log.w("DownloadDialog", "C'tor()");

        mContext = context;
        this.cancleDownLoad = cancleDownLoad;
        mResources = context.getResources();

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(mResources.getString(R.string.downloading));
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnKeyListener(onKeyListener);
    }

    // 返回键取消
    private OnKeyListener onKeyListener = new OnKeyListener() {

        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                Log.e(TAG, "返回键取消");
                cancel();
                if (cancleDownLoad != null) {
                    cancleDownLoad.cancle();
                }
            }
            return false;
        }
    };

    public void show() {
        try {
            mProgressDialog.show();
            Log.w("DownloadDialog", "show()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mProgressDialog.cancel();
            Log.w("DownloadDialog", "cancel()");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateState(DownloadStates downloadState) {
        try {
//			Log.w("DownloadDialog", "updateState(DownloadStates )");
            updateState(downloadState, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateState(DownloadStates downloadState, int progressValue) {
        try {
//			Log.w("DownloadDialog",
//					"updateState(DownloadStates, progressValue):"
//							+ progressValue);

            switch (downloadState) {
                case MESSAGE_DOWNLOAD_STARTING:
                    mProgressDialog.setIndeterminate(true);
                    mProgressDialog.setTitle(mResources
                            .getString(R.string.download_starting));
                    break;
                case MESSAGE_DOWNLOAD_PROGRESS:
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setTitle(mResources
                            .getString(R.string.downloading));
                    mProgressDialog.setProgress(progressValue);
                    break;
                case MESSAGE_DOWNLOAD_COMPLETE:
                    mProgressDialog.setProgress(0);
                    mProgressDialog.cancel();
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
