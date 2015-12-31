package com.rj.wisp.ui.pad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;

import com.rj.framework.DB;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrolImpl;
import com.rj.view.loading.CutsomProgressDialog;
import com.rj.wisp.R;
import com.rj.wisp.widget.AppSettingDialog;


public class PadHomeFragment extends Fragment {
    /**
     * Acitivity要实现这个接口，这样Fragment和Activity就可以共享事件触发的资源了
     */
    public interface PadHomeFragmentListener {
        void onWindowOpen(WebView webView);
    }

    private static final String TAG = PadHomeFragment.class.getName();
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
            Log.e(TAG, "loadUrl:" + url);
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
        System.out.println("PadHomeFragment--->onAttach");

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("PadHomeFragment--->onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("PadHomeFragment--->onCreateView");
        return inflater.inflate(R.layout.pad_home_fragment, container, false);
    }

    public PadHomeFragment() {
    }

    public PadHomeFragment(View parentView, String popHomePageUrl) {
        this.parentView = parentView;
        this.popHomePageUrl = popHomePageUrl;
    }

    private View parentView;
    private WebView homeWebView;
    private String popHomePageUrl;
    private WebViewCtrolImpl webViewCtrol;
    private FrameLayout homeMainLayout;

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("PadHomeFragment--->onResume");

        if (parentView == null) {
            return;
        }

        if (homeMainLayout == null) {
            homeMainLayout = (FrameLayout) parentView
                    .findViewById(R.id.home_frame_layout);
            if (!TextUtils.isEmpty(popHomePageUrl)) {
                homeMainLayout.post(new Runnable() {

                    @Override
                    public void run() {
                        homeMainLayout.setVisibility(View.VISIBLE);

                        Button settingBtn = (Button) parentView
                                .findViewById(R.id.settingBtn);
                        settingBtn.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                new AppSettingDialog(getActivity()).show();

                            }
                        });
                        homeWebView = (WebView) parentView
                                .findViewById(R.id.homeWebView);
                        homeWebView.setInitialScale(100);
                        homeWebView.getSettings().setJavaScriptEnabled(true);
                        homeWebView.setWebViewClient(new WebViewClient());
                        webViewCtrol = new WebViewCtrolImpl(getActivity());
                        homeWebView.setWebViewClient(new RjWebViewClient(
                                webViewCtrol));

                        homeWebView.getSettings().setBuiltInZoomControls(false);// jeozey
                        // 配合popwindow一定不能使用下面开关http://blog.csdn.net/changemyself/article/details/6969720
                        homeWebView.loadUrl(DB.PRE_URL
                                + popHomePageUrl.replace("adapter?open&url=",
                                ""));
                    }
                });

            }
        }
    }

    class PadHomeFragmentWebViewCtrol extends WebViewCtrolImpl {
        private CutsomProgressDialog loadDialog = null;

        public PadHomeFragmentWebViewCtrol(Activity activity, View parentView) {
            super(activity);
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            loadDialog = new CutsomProgressDialog(activity, dm.widthPixels,
                    parentView);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            try {
                if (loadDialog != null && !loadDialog.isShowing()) {
                    loadDialog.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.v("request", "onPageFinished:" + url);
            try {
                if (loadDialog != null && loadDialog.isShowing()) {
                    loadDialog.dismiss();
                }
            } catch (Exception e) {
            }
        }

        @Override
        public void onCreateWindow(WebView webview, boolean isUserGesture,
                                   Message resultMsg) {
            if (resultMsg == null) {
                return;
            }
            Log.e("NNN", "onCreateWindows1");
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            ((PadHomeFragmentListener) activity).onWindowOpen(webview);

            transport.setWebView(webview);
            resultMsg.sendToTarget();
            super.onCreateWindow(webview, isUserGesture, resultMsg);
        }
    }
}