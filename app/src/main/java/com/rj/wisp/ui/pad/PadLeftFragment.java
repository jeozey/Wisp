package com.rj.wisp.ui.pad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.rj.framework.webview.RjWebChromeClient;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrolImpl;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.button.CustomButton;
import com.rj.view.loading.CutsomProgressDialog;
import com.rj.wisp.R;

import java.util.List;


public class PadLeftFragment extends Fragment {
    /**
     * Acitivity要实现这个接口，这样Fragment和Activity就可以共享事件触发的资源了
     */
    public interface LeftFragmentListener {
        void onWindowOpen(WebView webView);

        void showSettingView();

        void showUserInfo();

        void initializeTabs(List<CustomButton> tablist);

        void logOut(String logOutEvent);

        void setLogOutEvent(String logOutEvent);
    }

    private static final String TAG = "PadLeftFragment";
    private Activity activity;
    private WebView webView;

    public void clearCache(boolean disk) {
        if (webView != null) {
            webView.clearCache(disk);
        }
    }

    public void destroyWebView() {
        try {
            if (webView != null) {
                webView.stopLoading();
                ViewGroup parent = (ViewGroup) webView.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
                webView.destroy();
                webView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void goBack() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        }
    }

    public void reload() {
        if (webView != null) {
            Log.e(TAG, "reload");
            webView.reload();
        }
    }

    public void loadUrl(String url) {
        try {
            Log.e(TAG, "webView:" + webView + "loadUrl:" + url);
            if (webView != null) {
                webView.loadUrl(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Fragment第一次附属于Activity时调用,在onCreate之前调用
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        System.out.println("LeftFragment--->onAttach");

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("LeftFragment--->onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("LeftFragment--->onCreateView");
        View view = inflater.inflate(R.layout.pad_left_fragment, container, false);

        if (frameLayout == null) {
            // titleTextView = (TextView) getView().findViewById(
            // R.id.leftTextTitle);

            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            loadDialog = new CutsomProgressDialog(activity, dm.widthPixels,
                    parentView);

            leftFragmentWebViewCtrol = new LeftFragmentWebViewCtrol(activity,
                    parentView);
            // webView = WebViewFactory.getNewWebView(activity,
            // "http://jeozey.sinaapp.com/readme.html");
            webView = WebViewFactory.getNewWebView(activity, url);
            webView.setWebViewClient(new RjWebViewClient(
                    leftFragmentWebViewCtrol));
            webView.setWebChromeClient(new RjWebChromeClient(activity,
                    leftFragmentWebViewCtrol));

            frameLayout = (FrameLayout) view.findViewById(
                    R.id.leftFrameLayout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            frameLayout.addView(webView, layoutParams);

        }
        return view;
    }

    private FrameLayout frameLayout;
    private View parentView;
    private String url;

    public PadLeftFragment() {
    }

    private boolean isHome;


    public PadLeftFragment(View parentView, String url, boolean isHome) {
        this.parentView = parentView;
        this.url = url;
        this.isHome = isHome;
    }

    private LeftFragmentWebViewCtrol leftFragmentWebViewCtrol;

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("LeftFragment--->onResume");
    }

    public void showLoading() {
        if (loadDialog != null && !loadDialog.isShowing()) {
            Log.e(TAG, "onPageStarted loadDialog.show()");
            loadDialog.show();
        }
    }

    public void dismissLoading() {
        try {
            if (loadDialog != null && loadDialog.isShowing()) {
                loadDialog.dismiss();
            }
        } catch (Exception e) {
        }
    }

    private CutsomProgressDialog loadDialog = null;

    public class LeftFragmentWebViewCtrol extends WebViewCtrolImpl {

        public LeftFragmentWebViewCtrol(Activity activity, View parentView) {
            super(activity);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {
                Log.e(TAG, "onPageStarted");
                if (loadDialog != null && !loadDialog.isShowing()) {
                    Log.e(TAG, "onPageStarted loadDialog.show()");
                    loadDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.v("request", "onPageFinished:" + url);
            dismissLoading();
        }

        @Override
        public void onCreateWindow(WebView webview, boolean isUserGesture,
                                   Message resultMsg) {
            if (resultMsg != null) {
                Log.e(TAG, "USER_AGENT:" + webview.getSettings().getUserAgentString());
                Log.e("NNN", "onCreateWindows1");
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                ((LeftFragmentListener) activity).onWindowOpen(webview);
                transport.setWebView(webview);
                resultMsg.sendToTarget();
            }
            super.onCreateWindow(webview, isUserGesture, resultMsg);
        }
    }
}