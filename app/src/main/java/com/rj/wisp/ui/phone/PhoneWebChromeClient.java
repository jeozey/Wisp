package com.rj.wisp.ui.phone;

import android.app.Activity;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.rj.framework.ButtonFactory;
import com.rj.framework.WISPComponentsParser;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.TopTabLayoutWidget;
import com.rj.view.button.CustomButton;
import com.rj.view.button.CustomWidgetButton;
import com.rj.view.button.PhoneHorizontalBtns;
import com.rj.wisp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PhoneWebChromeClient extends WebChromeClient {
    private static final String TAG = "PhoneWebChromeClient";
    private Activity activity;
    private RelativeLayout childLayout;
    private RelativeLayout browserLayout;
    private PhoneHorizontalBtns horizontalBtns;
    private TopTabLayoutWidget tabWidget;
    private LinkedList<WebView> webViews;
    private HashMap<Integer, ArrayList<CustomWidgetButton>> formButtons = new HashMap<Integer, ArrayList<CustomWidgetButton>>();
    private HashMap<Integer, ArrayList<CustomButton>> tabButtons = new HashMap<Integer, ArrayList<CustomButton>>();
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

    public void loadUrl(String url) {
        if (webView != null) {
            Log.e(TAG, "loadUrl:" + url);
            Log.i("_GuLang", "loadUrl url=" + url);
            webView.loadUrl(url);
        }
    }

    public void goBack() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        }
    }

    public void reload() {
        if (webView != null) {
            webView.reload();
        }
    }

    public PhoneWebChromeClient(Activity activity, RelativeLayout wrappingLayout,
                                RelativeLayout browserLayout) {
        this.activity = activity;
        this.childLayout = wrappingLayout;
        this.browserLayout = browserLayout;
        webViews = new LinkedList<WebView>();
    }

    private List<CustomButton> collectionlist;// 更多按钮

    // 初始化动态按钮
    public void updateBottomTabBar(String data, PhoneHorizontalBtns horizontalBtns, TopTabLayoutWidget tabWidget) {
//		Log.i("_GuLang","updateBottomButtonBar  -->"+data);
        try {
            this.horizontalBtns = horizontalBtns;
            this.tabWidget = tabWidget;

            Log.e(TAG, "buttonsJson:" + data);
            // updateBottomButtonBar(buttonsJson);

            Map<String, List> map = WISPComponentsParser
                    .getCustomButtonList4Json(data);
            // 记录按钮数字
            int buttonNum = 0;

            collectionlist = map.get("collectionlist");
            List<CustomButton> buttonlist = map.get("bottombtnlist");

            ArrayList<CustomButton> tablist = (ArrayList<CustomButton>) map.get("tabslist");
            initializeTabs(tablist, tabWidget);
            tabButtons.put(webView.hashCode(), tablist);// 保存form页面的tab按钮

//			horizontalBtns = (PhoneHorizontalBtns) activity
//					.findViewById(R.id.form_bottom_navigate_bar);
            ArrayList<CustomWidgetButton> list = new ArrayList<CustomWidgetButton>();
            CustomWidgetButton info;
            CustomWidgetButton backBtn = null;
            for (CustomButton customButton : buttonlist) {
                if ("backbtn".equalsIgnoreCase(customButton.getType())) {
                    backBtn = new CustomWidgetButton();
                    backBtn.setType(CustomWidgetButton.ButtonType.LeftBtn);
                    backBtn.setTitle(customButton.getButtontext());
//					backBtn.setBeforeImg(activity.getResources().getDrawable(
//							R.drawable.return_icon60));
                    backBtn.setBeforeImg(ButtonFactory.getDrawable(activity, customButton.getBeforeimg()));
                    backBtn.setAfterImg(ButtonFactory.getDrawable(activity, customButton.getAfterimg()));
                    backBtn.setCallBack(customButton.getClickevent());
                    list.add(backBtn);
                } else if ("operationbtn".equalsIgnoreCase(customButton.getType())) {
                    info = new CustomWidgetButton();
                    info.setBeforeImg(ButtonFactory.getDrawable(activity,
                            customButton.getBeforeimg()));
                    info.setAfterImg(ButtonFactory.getDrawable(activity, customButton.getAfterimg()));
                    try {
                        info.setNum(Integer.parseInt(customButton.getNumber()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        info.setNum(0);
                    }
                    info.setTitle(customButton.getButtontext());
                    info.setCallBack(customButton.getClickevent());
                    info.setIsclick(customButton.getIsclick());
                    list.add(info);
                }
            }
            formButtons.put(webView.hashCode(), list);// 保存form页面的底部按钮

            if (list == null || list.size() == 0) {
                horizontalBtns.setVisibility(View.GONE);
            } else {
                horizontalBtns.setVisibility(View.VISIBLE);
                initHorizontalBtns(horizontalBtns, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeTabs(List<CustomButton> tablist, TopTabLayoutWidget tabWidget) {
        try {
            this.tabWidget = tabWidget;
//			final TopTabLayoutWidget tabWidget = (TopTabLayoutWidget) activity.findViewById(R.id.tabLayoutWidget);
            Log.e(TAG, "initializeTabs");
            tabWidget.setVisibility(View.VISIBLE);
            List<CustomWidgetButton> buttons = new ArrayList<CustomWidgetButton>();
            CustomWidgetButton btn = new CustomWidgetButton();
            for (CustomButton button : tablist) {
                Log.e(TAG, "button:" + button);
                btn = new CustomWidgetButton();
                btn.setTitle(button.getButtontext());
                btn.setCallBack(button.getClickevent());
                btn.setIsclick(button.getIsclick());
                buttons.add(btn);
            }
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

            tabWidget.init(mDisplayMetrics.widthPixels, buttons, new TopTabLayoutWidget.ITabLayoutWidget() {

                @Override
                public void callBack(String callBack) {
//					Toast.makeText(activity, callBack, Toast.LENGTH_SHORT).show();
                    if (!TextUtils.isEmpty(callBack)) {
                        loadUrl(callBack);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initHorizontalBtns(PhoneHorizontalBtns horizontalBtns, ArrayList<CustomWidgetButton> list) {
//		Log.i("_GuLang","initHorizontalBtns   list"+list);
        try {
            horizontalBtns.init(list, new PhoneHorizontalBtns.HorizontalBtnsCallBack() {
                @Override
                public void callBack(CustomWidgetButton info) {
                    if (CustomWidgetButton.ButtonType.LeftBtn == info.getType()) {
                        //执行返回的js 再关闭窗口（返回按钮不建议使用，解锁事件可以用 先解锁后关闭）
                        loadUrl(info.getCallBack());
                        closeChild();
                    } else {
                        Log.i("_GuLang", "Item回调222  " + info.getCallBack());
                        if (!TextUtils.isEmpty(info.getCallBack())) {
                            loadUrl(info.getCallBack());
                        }
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message,
                             JsResult result) {
        WebViewCtrol a = (WebViewCtrol) activity;
        a.onJsAlert(view, url, message, result);
        return true;
    }

    @Override
    public void onCloseWindow(WebView window) {
        Log.e("NNN", "onCloseWindow");
        if (webViews.size() > 1) {
            Log.e("NNN", "back to parent:" + webViews.size());
            WebView wb = webViews.pop();
            browserLayout.removeAllViews();
            ViewGroup parent = (ViewGroup) wb.getParent();
            if (parent != null) {
                parent.removeAllViews();
            }
            browserLayout.addView(wb);


            //恢复底部按钮
            ArrayList<CustomWidgetButton> buttons = formButtons.get(wb.hashCode());
            if (buttons != null) {
                initHorizontalBtns(horizontalBtns, buttons);
            }
            //恢复tab标签
            ArrayList<CustomButton> tabBtns = tabButtons.get(wb
                    .hashCode());
            if (tabBtns != null) {
                initializeTabs(tabBtns, tabWidget);
            }
            //给当前webview赋值
            webView = wb;
//			if(wb!=null){
//				//重新生成顶部和底部的按钮栏（用于返回上一步  但是全部window.close的时候也调用了一次 浪费了 ）
//				wb.reload();
//			}
        } else {
            closeChild();
        }
        super.onCloseWindow(window);
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {

        Log.e(TAG, "onCreateWindow:" + view);
        // remove any current child views
        browserLayout.removeAllViews();
        // make the child web view's layout visible
        childLayout.setVisibility(View.VISIBLE);

        //发现部分手机会重复调用onCreateWindow
        if (!webViews.contains(view)) {
            webViews.push(view);
        }


        // now create a new web view
        webView = WebViewFactory.getNewWebView(activity, null);
        webView.setWebChromeClient(this);
        webView.setWebViewClient(new RjWebViewClient((WebViewCtrol) activity));
        // add the new web view to the layout
        webView.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        browserLayout.addView(webView);
        // tell the transport about the new view
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(webView);
        resultMsg.sendToTarget();

        // let's be cool and slide the new web view up into view
        Animation slideUp = AnimationUtils.loadAnimation(activity,
                R.anim.slide_left);
        childLayout.startAnimation(slideUp);
        return true;
    }

    /**
     * Lower the child web view down
     */
    public void closeChild() {
        Animation slideDown = AnimationUtils.loadAnimation(activity,
                R.anim.slide_right);
        childLayout.startAnimation(slideDown);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                childLayout.setVisibility(View.INVISIBLE);
                webViews.clear();
                formButtons.clear();
                tabButtons.clear();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public boolean isChildOpen() {
        return childLayout.getVisibility() == View.VISIBLE;
    }
}