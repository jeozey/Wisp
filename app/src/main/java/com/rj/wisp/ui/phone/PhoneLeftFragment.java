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
import android.widget.Toast;

import com.rj.framework.DB;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.TopTabLayoutWidget;
import com.rj.view.button.PhoneHorizontalBtns;
import com.rj.wisp.R;
import com.rj.wisp.base.WispApplication;


public class PhoneLeftFragment extends Fragment {
    private static final String TAG = "PhoneLeftFragment";
    private Activity activity;
    private WebView webView;
    PhoneWebChromeClient phoneWebChromeClient;

    public void closeFormView() {
        if (phoneWebChromeClient != null && phoneWebChromeClient.isChildOpen()) {
            phoneWebChromeClient.closeChild();
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

    //	public void initializeTabs(List<CustomButton> tablist) {
//		if(phoneWebChromeClient!=null){
//			final TopTabLayoutWidget tabWidget = (TopTabLayoutWidget) activity.findViewById(R.id.tabLayoutWidget);
//			phoneWebChromeClient.initializeTabs(tablist,tabWidget);
//		}
//
//	}
    public void updateBottomTabBar(String data) {
        if (phoneWebChromeClient != null) {
            final PhoneHorizontalBtns horizontalBtns = (PhoneHorizontalBtns) activity
                    .findViewById(R.id.form_bottom_navigate_bar);
            final TopTabLayoutWidget tabWidget = (TopTabLayoutWidget) activity.findViewById(R.id.tabLayoutWidget);
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
            phoneWebChromeClient.reload();
        } else {
            if (webView != null) {
                webView.reload();
            }
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
        return inflater.inflate(R.layout.phone_left_fragment, container, false);
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

            // LeftFragmentWebViewCtrol leftFragmentWebViewCtrol = new
            // LeftFragmentWebViewCtrol(
            // activity);
            Log.e(TAG, "WispApplication.cookies:" + WispApplication.cookies);
//			synCookies(activity, DB.PRE_URL
//						+ DB.HOMEPAGE_URL, WispApplication.cookies);
            Log.e(TAG, "DB.LOGINPAGE_URL" + DB.APP_URL);
            Log.e(TAG, "DB.HOMEPAGE_URL" + DB.HOMEPAGE_URL);
            if (webView == null) {
                webView = WebViewFactory.getNewWebView(activity, DB.PRE_URL
                        + DB.HOMEPAGE_URL);
                // webView = WebViewFactory.getNewWebView(activity,
                // "http://jeozey.sinaapp.com/readme.html");
                // webView.setWebViewClient(new
                // RjWebViewClient(leftFragmentWebViewCtrol));
                // webView.setWebChromeClient(new RjWebChromeClient(activity,
                // leftFragmentWebViewCtrol));

                RelativeLayout browserLayout = (RelativeLayout) getActivity()
                        .findViewById(R.id.mainBrowserLayout);
                RelativeLayout childLayout = (RelativeLayout) getActivity()
                        .findViewById(R.id.right_fragment_layout);

                //设置webview
                phoneWebChromeClient = new PhoneWebChromeClient(activity,
                        childLayout, browserLayout);
                webView.setWebChromeClient(phoneWebChromeClient);
                webView.setWebViewClient(new RjWebViewClient(
                        (WebViewCtrol) activity));


                LinearLayout leftLinearLayout = (LinearLayout) getActivity()
                        .findViewById(R.id.leftLinearLayout);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                leftLinearLayout.addView(webView, layoutParams);
            }

        } catch (ClassCastException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "转换异常", Toast.LENGTH_SHORT).show();
        }
    }

}