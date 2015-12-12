package com.rj.wisp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rj.framework.DB;
import com.rj.framework.webview.RjWebChromeClient;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewCtrolImpl;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.ToastTool;
import com.rj.wisp.R;
import com.rj.wisp.base.BaseActivity;
import com.rj.wisp.base.WispApplication;
import com.rj.wisp.core.ServiceThread;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.ui.pad.PadMainActivity;
import com.rj.wisp.ui.phone.PhoneMainActivity;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private WebView webView;
    private String popHomePageUrl = "";
    // private LoginSuccessBroadcastReceiver receiver;

    private void loginSuccess(final String jsonStr) {
        ToastTool.show(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT);
        // CookieSyncManager.createInstance(this);
        // CookieSyncManager.getInstance().sync();

        WispApplication.isLogin = true;
        //获取cookies
        String cookies = CookieManager.getInstance().getCookie(DB.PRE_URL + DB.LOGINPAGE_URL);
        WispApplication.cookies = cookies;

        // 登陆时候跳转多个地方， 所以暂时不停止， 让他继续跳转，
        // 到时候如果出现登陆不行的情况, 再考虑loginsuccess之后的数据都写入主页面的webview中
        // webView.stopLoading();
        // webView.loadUrl(DB.PRE_URL+DB.LOGINPAGE_URL);
        WispCore.getWISPSO().CloseService();

        if (DB.isPhone) {
            Intent intent = new Intent(LoginActivity.this, PhoneMainActivity.class);
            intent.putExtra("success", "true");
            intent.putExtra("popHomePageUrl", popHomePageUrl);
            startActivityForResult(intent, 1);
        } else {
            Intent intent = new Intent(LoginActivity.this, PadMainActivity.class);
            intent.putExtra("success", "true");
            intent.putExtra("popHomePageUrl", popHomePageUrl);
            startActivityForResult(intent, 1);
        }

        finish();

    }

    private void showConfirmDialog(Message msg) {
//		try {
//			String type = msg.getData().getString("type");
//			String content = msg.getData().getString("text");
//			String title = msg.getData().getString("title");
//			Log.e("title", title + "1111");
//			final String callBack = msg.getData().getString("callBack");
//			Dialog dialog = WispDialogUtils.onCreateDialog(LoginActivity.this, type, content, title, new IDialogChoose() {
//
//				@Override
//				public void callBack(ConfirmResult result) {
//					if (result == ConfirmResult.CONFIRM_YES) {
//					} else {
//
//					}
//
//				}
//			});
//			dialog.show();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

    }

    private String CACallBack = "";
    private Handler handler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.e(TAG, "msg.what:" + msg.what);
            switch (msg.what) {
                case ServiceThread.ADD_WEB_UI:
                    if (msg.obj != null) {
                        String jsonStr = msg.obj.toString();
                        Log.e(TAG, "jsonStr:" + jsonStr);
                        // 有注销按钮则说明登陆成功
                        if (!TextUtils.isEmpty(jsonStr) && jsonStr.indexOf("logout") != -1) {
                            loginSuccess(jsonStr);
                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        WispApplication.isLogin = false;
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate:" + handler);
        super.onCreate(savedInstanceState);


        // 奔溃的情况
        if (TextUtils.isEmpty(DB.SECURITY_HOST)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        try {
            WispCore.getWISPSO().StartService(handler, getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // AppSystemTool.clearWebViewCookie(getBaseContext());
        Log.e(TAG, "DB.LOGINPAGE_URL:" + DB.LOGINPAGE_URL);
        try {
            String url = getIntent().getStringExtra("url");
//			url = "http://baidu.com";
            if (!TextUtils.isEmpty(url)) {
                webView = WebViewFactory.getNewWebView(LoginActivity.this, url);

            } else
//				webView = WebViewFactory.getNewWebView(LoginActivity.this, "http://127.0.0.1:8011/wisp_aas/ClientInfo.jsp?url=http%3A%2F%2F192.168.1.12%2Fhomepage.nsf");
                webView = WebViewFactory.getNewWebView(LoginActivity.this, DB.PRE_URL + DB.LOGINPAGE_URL);
            clearCache(webView, true);// 清除下缓存
        } catch (Exception e) {
            webView = WebViewFactory.getNewWebView(LoginActivity.this, DB.PRE_URL + DB.LOGINPAGE_URL);
        }


        boolean isUseroff = getIntent().getBooleanExtra("isUserOff", false);
        if (isUseroff) {
//        	ToastTool.show(LoginActivity.this, "正在注销..", Toast.LENGTH_LONG);
            final String logoutEvent = getIntent().getStringExtra("logoutEvent");
            Log.e(TAG, "注销 logoutEvent:" + logoutEvent);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "logoutEvent:" + logoutEvent);
                    if (!TextUtils.isEmpty(logoutEvent)) {
                        webView.loadUrl(logoutEvent);
                    }
                }
            }, 1000);
        }

        // webView = WebViewFactory.getNewWebView(LoginActivity.this,
        // "http://jeozey.sinaapp.com/readme.html");
        WebViewCtrol webViewCtrol = new WebViewCtrolImpl(LoginActivity.this);
        webView.setWebViewClient(new RjWebViewClient(webViewCtrol));
        webView.setWebChromeClient(new RjWebChromeClient(LoginActivity.this, webViewCtrol));

        LinearLayout leftLinearLayout = (LinearLayout) findViewById(R.id.loginLinearLayout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        leftLinearLayout.addView(webView, layoutParams);

    }


    @Override
    /**
     * 拦截MENU
     */
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (tabMenu != null) {
            if (tabMenu.isShowing())
                tabMenu.dismiss();
            else {
                tabMenu.showAtLocation(findViewById(R.id.loginLinearLayout),
                        Gravity.BOTTOM, 0, 0);
            }
        }
        return false;// 返回为true 则显示系统menu
    }

    @Override
    protected void onTapMenuSelect(int position) {
        switch (position) {
            case 0:
                if (webView != null) {
                    webView.loadUrl(DB.PRE_URL + DB.LOGINPAGE_URL);
                }
                break;
            case 1:
                clearCache(webView, true);
                clearCookies();
                webView.loadUrl(DB.PRE_URL + DB.LOGINPAGE_URL);
                break;
            case 2:
//			if (DB.isPhone) {
//				startActivity(new Intent(LoginActivity.this,
//						SettingActivity.class));
//			} else {
//				new AppSettingDialog(this).show();
//			}
                break;
            case 3:
                exitApp();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        // if (receiver != null) {
        // unregisterReceiver(receiver);
        // }
        Log.e(TAG, "onDestroy");
        // 清除缓存
        clearCache(webView, true);
        super.onDestroy();
    }


}
