package com.rj.framework.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.io.Serializable;

public class RJWebView extends WebView implements Serializable {
    private static final String TAG = RJWebView.class.getName();
    private boolean is_gone = false;

    public RJWebView(Context context) {
        super(context);
    }

    public RJWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.GONE) {
            try {
                WebView.class.getMethod("onPause").invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(TAG, "pauseTimers");
            this.pauseTimers();
            this.is_gone = true;
        } else if (visibility == View.VISIBLE) {
            try {
                WebView.class.getMethod("onResume").invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(TAG, "resumeTimers");
            this.resumeTimers();
            this.is_gone = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.is_gone) {
            try {
                this.destroy();
            } catch (Exception e) {

            }
        }
    }
}
