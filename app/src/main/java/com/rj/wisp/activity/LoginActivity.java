package com.rj.wisp.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.rj.framework.webview.RjWebChromeClient;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewCtrolImpl;
import com.rj.framework.webview.WebViewFactory;
import com.rj.util.ToastTool;
import com.rj.wisp.R;
import com.rj.wisp.base.BaseActivity;
import com.rj.wisp.base.WispApplication;
import com.rj.wisp.core.DB;
import com.rj.wisp.core.ServiceThread;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.ui.pad.PadMainActivity;
import com.rj.wisp.ui.phone.PhoneMainActivity;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@SuppressLint("NewApi")
public class LoginActivity extends BaseActivity implements KeyChainAliasCallback {
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
        String cookies = CookieManager.getInstance().getCookie(DB.PRE_URL + DB.APP_URL);
        WispApplication.cookies = cookies;

        // 登陆时候跳转多个地方， 所以暂时不停止， 让他继续跳转，
        // 到时候如果出现登陆不行的情况, 再考虑loginsuccess之后的数据都写入主页面的webview中
        // webView.stopLoading();
        // webView.loadUrl(DB.PRE_URL+DB.APP_URL);
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
                case 1:
                    // if(webView!=null&&webView.canGoBack()){
                    // webView.goBack();
                    // }
                    webView.loadUrl(DB.PRE_URL + DB.APP_URL);
                    break;
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
                case 23:
                    if (msg.getData() != null) {
                        try {
                            JSONObject json = JSONObject.parseObject(msg.getData().getString("data"));
                            CACallBack = json.getString("callback");
                            SourceData = json.getString("sourcedata");
                            if (TextUtils.isEmpty(CACallBack)) {
                                return false;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            AppCode = "1013";
                            KeyChain.choosePrivateKeyAlias(LoginActivity.this, LoginActivity.this, new String[]{"RSA"}, null, null, -1, null);
                        } catch (Exception e) {
                            ToastTool.show(LoginActivity.this, "获取CA认证失败", 1);
                        }

                    }
                    break;
                case 4:
                    if (msg.obj != null) {
                        doAlias(msg.obj.toString());
                    }
                    break;
                case 5:
                    try {
                        String homePage = msg.obj.toString();
                        if (!TextUtils.isEmpty(homePage) && homePage.startsWith("http")) {
                            DB.HOMEPAGE_URL = homePage;
                            Log.e(TAG, "DB.HOMEPAGE_URL:" + DB.HOMEPAGE_URL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 22:
                    showConfirmDialog(msg);
                    break;
                case 30:
                    final String showText = msg.getData().getString("text");
                    if (!TextUtils.isEmpty(showText)) {
                        ToastTool.show(LoginActivity.this, showText, Toast.LENGTH_SHORT);
                    }
                    break;
                case 120:
                    try {
                        //弹出主页面
                        if (msg.obj != null) {
                            popHomePageUrl = String.valueOf(msg.obj);
                            Log.e(TAG, "popHomePageUrl:" + popHomePageUrl);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data != null) {
                    Log.e(TAG, "resultCode:" + resultCode);
                    if (resultCode == 1) {// 注销
                        ToastTool.show(LoginActivity.this, "正在注销..", Toast.LENGTH_LONG);
                        try {
                            WispCore.getWISPSO().StartService(handler, getBaseContext());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        final String logoutEvent = data.getStringExtra("logoutEvent");
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

                    } else {// 退出
                        WispCore.getWISPSO().CloseService();

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        }, 1000);
                    }

                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        WispApplication.isLogin = false;
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
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
        Log.e(TAG, "DB.APP_URL:" + DB.APP_URL);
        try {
            String url = getIntent().getStringExtra("url");
//			url = "http://baidu.com";
            if (!TextUtils.isEmpty(url)) {
                webView = WebViewFactory.getNewWebView(LoginActivity.this, url);

            } else
//				webView = WebViewFactory.getNewWebView(LoginActivity.this, "http://127.0.0.1:8011/wisp_aas/ClientInfo.jsp?url=http%3A%2F%2F192.168.1.12%2Fhomepage.nsf");
                webView = WebViewFactory.getNewWebView(LoginActivity.this, DB.PRE_URL + DB.APP_URL);
            clearCache(webView, true);// 清除下缓存
        } catch (Exception e) {
            webView = WebViewFactory.getNewWebView(LoginActivity.this, DB.PRE_URL + DB.APP_URL);
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

        // receiver = new LoginSuccessBroadcastReceiver();
        // IntentFilter filter = new IntentFilter(BROAD_CAST_NAME); //
        // 和广播中Intent的action对应
        // registerReceiver(receiver, filter);


//		try {
//			AppCode = "1013";
//			KeyChain.choosePrivateKeyAlias(LoginActivity.this, LoginActivity.this, new String[] { "RSA" }, null, null, -1, null);
//		} catch (Exception e) {
//			ToastTool.show(LoginActivity.this, "获取CA认证失败", 1);
//		}

//		AjaxGetResourcesTask ajaxGetResourcesTask = new AjaxGetResourcesTask(
//				this, handler);
//		ajaxGetResourcesTask.execute(0);
    }

    private static final String BROAD_CAST_NAME = "com.rj.LoginActivity.loginSuccess";

    // 采用广播监听登陆成功信号
    class LoginSuccessBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "LoginSuccessBroadcastReceiver");
            if (intent.getAction().equals(BROAD_CAST_NAME)) {
                // loginSuccess("");
            }
        }
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
                    webView.loadUrl(DB.PRE_URL + DB.APP_URL);
                }
                break;
            case 1:
                clearCache(webView, true);
                clearCookies();
                webView.loadUrl(DB.PRE_URL + DB.APP_URL);
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

	/*@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (webView != null) {
				webView.loadUrl(DB.PRE_URL + DB.APP_URL);
			}
			return false;
		case 1:
			clearCache(webView, true);
			clearCookies();
			webView.loadUrl(DB.PRE_URL + DB.APP_URL);

			return false;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}*/

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


    private String SignData, SignCert, SourceData, AppCode, UserName;

    @Override
    public void alias(final String alias) {
        Log.d(TAG, "Thread: " + Thread.currentThread().getName());
        Log.d(TAG, "selected alias: " + alias);
        Message msg = handler.obtainMessage(4);
        msg.obj = alias;
        handler.sendMessage(msg);

    }

    private PrivateKey pk;

    private void doAlias(final String alias) {
        final Context ctx = LoginActivity.this;
        final StringBuffer _view = new StringBuffer();
//		Runnable r = new Runnable() {
//			public void run() {
        try {

            _view.append("发现密钥: " + alias + "\r\n");
            new AsyncTask<Void, String, Boolean[]>() {
                private Exception error;

                @Override
                protected void onPreExecute() {

//							setProgressBarIndeterminateVisibility(true);
                    _view.append("开始签名并验证用户证书（自签自验）" + "\r\n");
                }

                @Override
                protected Boolean[] doInBackground(Void... arg0) {
                    try {
                        pk = KeyChain.getPrivateKey(ctx, alias);
                        X509Certificate[] chain = KeyChain.getCertificateChain(ctx, alias);
                        Log.d(TAG, "chain length: " + chain.length + "\r\n");
                        for (X509Certificate certificate : chain) {
                            Log.i(TAG, "Subject DN:" + certificate.getSubjectDN().getName() + "\r\n");
                            String s = certificate.toString();
                            byte[] b = certificate.getExtensionValue("1.2.86.11.7.11");
                            if (b == null) {
                                continue;
                            }
                            byte[] t = new byte[b.length];
                            int j = 0;
                            for (int i = 0; i < t.length; i++) {
                                if ((int) b[i] > 32)
                                    t[j++] = b[i];
                            }
                            UserName = new String(t, 0, j);
                            Log.e(TAG, "UserName:" + UserName);
                            Log.i(TAG, "Issuer DN:" + certificate.getIssuerDN().getName() + "\r\n");
                        }


                        Boolean[] result = new Boolean[2];
                        byte[] data = SourceData.getBytes("GBK");
                        Signature sig = Signature.getInstance("SHA1withRSA");
                        sig.initSign(pk);
                        sig.update(data);
                        byte[] signed = sig.sign();
                        SignData = Base64.encodeToString(signed, Base64.NO_WRAP);
                        byte[] certBuf = chain[0].getEncoded();
                        SignCert = Base64.encodeToString(certBuf, Base64.NO_WRAP);
                        PublicKey pubk = chain[0].getPublicKey();
                        sig.initVerify(pubk);
                        sig.update(data);
                        boolean valid = sig.verify(signed);

                        Log.d(TAG, "signature is valid: " + valid);

                        result[0] = valid;
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
                        Log.d(TAG, "TrustManagerFactory provider " + tmf.getProvider().getName());

                        tmf.init((KeyStore) null);
                        TrustManager[] tms = tmf.getTrustManagers();

                        Log.d(TAG, "num trust managers: " + tms.length + "\r\n");
                        X509TrustManager xtm = (X509TrustManager) tms[0];

                        Log.d(TAG, "checking chain with " + xtm + "\r\n");
                        try {
                            xtm.checkClientTrusted(chain, "RSA");

                            Log.d(TAG, "chain is valid" + "\r\n");
                            result[1] = true;
                        } catch (CertificateException ce) {
                            Log.e(TAG, "Error validating certificate chain.", ce);

                            result[1] = false;
                        }

                        Log.d(TAG, "SignData:" + SignData);

                        return result;
                    } catch (Exception e) {
                        Log.e(TAG, "Error using private key.", e);

                        error = e;
                        return null;
                    }

                }

                protected void onPostExecute(Boolean[] valid) {
//							setProgressBarIndeterminateVisibility(false);
                    if (valid == null && error != null) {
                        _view.append("Error: " + error.getMessage() + "\r\n");
                        return;
                    }
                    boolean signatureValid = valid[0];
                    boolean certTrusted = valid[1];
                    String message = String.format("验签是否通过: %s, 证书链是否验证通过: %s", signatureValid, certTrusted);
                    _view.append(message + "\r\n");
                    if (signatureValid) {
                        new MyAsyncTask().execute("", "");
                    } else {
                        ToastTool.show(LoginActivity.this, "验签不通过", 1);
                    }
                }

                protected void onProgressUpdate(String... values) {
                    _view.append(values + "\r\n");
                }

            }.execute();

        } catch (Exception e) {
            Log.e(TAG, "Error getting private key.", e);
            _view.append(e.getMessage() + "\r\n");
        }

//			}
//		};
//		runOnUiThread(r);
//		handler.post(r);
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String s = postData(params);
            return s;
        }

        protected void onPostExecute(String result) {
//			int _r = Integer.parseInt(result.trim());
            String msg = "";
//			if (_r == 0)
            msg = ("成功获取客户端证书" + "\r\n");
//			else if (_r > 9000) {
//				int days = _r - 9000;
//				msg = ("客户端证书验证成功,费用到期时间仅剩" + days + "天" + "\r\n");
//			} else if (_r > 0 && _r < 90) {
//				msg = ("客户端证书验证成功,距到期时间仅剩" + result + "天，请及时延期" + "\r\n");
//			} else
//				msg = ("客户端证书验证失败，错误代码:" + result + "\r\n");
//
            ToastTool.show(LoginActivity.this, msg, 1);
        }

        public String postData(String valueIWantToSend[]) {
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    Log.e(TAG, "CACallBack:" + CACallBack);
                    if (!TextUtils.isEmpty(CACallBack)) {
                        String callBack = "javascript:try{" + CACallBack
                                + "('" + SignCert + "','" + SignData + "','" + SourceData + "','" + UserName + "');}catch(e){}";
                        Log.e(TAG, "callBack:" + callBack);
                        webView.loadUrl(callBack);
                    }
                    String postString = String.format("signcert=%s&signdata=%s&sourcedata=%s&appcode=%s", Uri.encode(SignCert), Uri.encode(SignData), SourceData, AppCode);
                    Log.d(TAG, "data:" + postString);
                }
            };
            runOnUiThread(r);


            return "";
        }
    }
}
