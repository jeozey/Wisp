package com.rj.wisp.ui.phone;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.rj.framework.DB;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.SlideTitle;
import com.rj.view.button.PhoneHorizontalBtns;
import com.rj.wisp.R;


public class PhoneLeftFragment extends Fragment {
    private static final String TAG = "PhoneLeftFragment";
    private Context context;
    private WebView webView;
    PhoneWebChromeClient phoneWebChromeClient;

    public void closeFormView() {
        if (phoneWebChromeClient != null && phoneWebChromeClient.isChildOpen()) {
            phoneWebChromeClient.closeCurrentWebView();
//            phoneWebChromeClient.closeChild();
        }
    }

    public void clearCache(boolean disk) {
        if (webView != null) {
            webView.clearCache(disk);
        }
        if (phoneWebChromeClient != null) {
            phoneWebChromeClient.clearCache(disk);
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
            if (phoneWebChromeClient != null) {
                phoneWebChromeClient.destroyWebView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateBottomTabBar(String data) {
        if (phoneWebChromeClient != null) {

            phoneWebChromeClient.updateBottomTabBar(data, horizontalBtns, tabWidget);
        }

    }

    public Boolean isFormViewOpen() {
        return phoneWebChromeClient != null && phoneWebChromeClient.isChildOpen();
    }

    public void loadUrl(String url) {
        if (phoneWebChromeClient != null && phoneWebChromeClient.isChildOpen()) {
            phoneWebChromeClient.loadUrl(url);
        } else {
            if (webView != null) {
                webView.loadUrl(url);
            }
        }
    }

    public void goBack() {
        if (phoneWebChromeClient != null && phoneWebChromeClient.isChildOpen()) {
            phoneWebChromeClient.goBack();
        } else {
            if (webView != null) {
                webView.goBack();
            }
        }
    }

    public void reload() {
        if (phoneWebChromeClient != null && phoneWebChromeClient.isChildOpen()) {
            Log.e(TAG, "phoneWebChromeClient reload");
            phoneWebChromeClient.reload();
        } else {
            if (webView != null) {
                Log.e(TAG, "webView reload");
                webView.reload();
            }
        }
    }

    /**
     * Fragment第一次附属于Activity时调用,在onCreate之前调用
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println("LeftFragment--->onAttach");

        this.context = context;
        horizontalBtns = (PhoneHorizontalBtns) ((Activity) context)
                .findViewById(R.id.form_bottom_navigate_bar);
        tabWidget = (SlideTitle) ((Activity) context).findViewById(R.id.tabLayoutWidget);
//        tabWidget = (TopTabLayoutWidget) ((Activity) context).findViewById(R.id.tabLayoutWidget);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("LeftFragment--->onCreate");
    }

    private PhoneHorizontalBtns horizontalBtns;
    private SlideTitle tabWidget;

    //    private TopTabLayoutWidget tabWidget;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("LeftFragment--->onCreateView");
        View view = inflater.inflate(R.layout.phone_left_fragment, container, false);

        return view;
    }

    /**
     * 同步一下cookie
     */
    public static void synCookies(Context context, String url, String cookies) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();//移除
        cookieManager.setCookie(url, cookies);//cookies是在HttpClient中获得的cookie
        CookieSyncManager.getInstance().sync();
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("LeftFragment--->onResume");

        try {

            if (webView == null) {
//                webView = WebViewFactory.getNewWebView(context, DB.PRE_URL
//                        + DB.HOMEPAGE_URL);

                final PullToRefreshWebView pullRefreshWebView = new PullToRefreshWebView(context);
                pullRefreshWebView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<WebView>() {
                    @Override
                    public void onRefresh(PullToRefreshBase<WebView> refreshView) {
                        webView.reload();
                        pullRefreshWebView.onRefreshComplete();
                    }
                });
                webView = pullRefreshWebView.getRefreshableView();
                WebViewFactory.initWebView(context, webView);

                RelativeLayout browserLayout = (RelativeLayout) getActivity()
                        .findViewById(R.id.mainBrowserLayout);
                RelativeLayout childLayout = (RelativeLayout) getActivity()
                        .findViewById(R.id.right_fragment_layout);

                //设置webview
                phoneWebChromeClient = new PhoneWebChromeClient(context,
                        childLayout, browserLayout);
                webView.setWebChromeClient(phoneWebChromeClient);
                webView.setWebViewClient(new RjWebViewClient(
                        (WebViewCtrol) context));


                webView.loadUrl(DB.PRE_URL + DB.LOGINPAGE_URL);
                LinearLayout leftLinearLayout = (LinearLayout) getActivity()
                        .findViewById(R.id.leftLinearLayout);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                leftLinearLayout.addView(pullRefreshWebView, layoutParams);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}