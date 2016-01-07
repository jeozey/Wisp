package com.rj.wisp.ui.phone;

import android.content.Context;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.rj.framework.ButtonFactory;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.SlideItem;
import com.rj.view.SlideTitle;
import com.rj.view.button.CustomButton;
import com.rj.view.button.CustomWidgetButton;
import com.rj.view.button.PhoneHorizontalBtns;
import com.rj.wisp.R;
import com.rj.wisp.bean.CustomBtn;
import com.rj.wisp.core.WISPComponentsParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PhoneWebChromeClient extends WebChromeClient {
    private static final String TAG = "PhoneWebChromeClient";
    private Context context;
    private RelativeLayout childLayout;
    private RelativeLayout browserLayout;
    private PhoneHorizontalBtns horizontalBtns;
    private SlideTitle tabWidget;
    private LinkedList<WebView> webViews;
    private HashMap<WebView, PullToRefreshWebView> refreshWebviews = new HashMap<>();
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

    public PhoneWebChromeClient(Context context, RelativeLayout wrappingLayout,
                                RelativeLayout browserLayout) {
        this.context = context;
        this.childLayout = wrappingLayout;
        this.browserLayout = browserLayout;
        webViews = new LinkedList<WebView>();
    }

    private List<CustomButton> collectionlist;// 更多按钮

    // 初始化动态按钮
    public void updateBottomTabBar(String data, PhoneHorizontalBtns horizontalBtns, SlideTitle tabWidget) {
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

//			horizontalBtns = (PhoneHorizontalBtns) context
//					.findViewById(R.id.form_bottom_navigate_bar);
            ArrayList<CustomWidgetButton> list = new ArrayList<CustomWidgetButton>();
            CustomWidgetButton info;
            CustomWidgetButton backBtn = null;
            for (CustomButton customButton : buttonlist) {
                if ("backbtn".equalsIgnoreCase(customButton.getType())) {
                    backBtn = new CustomWidgetButton();
                    backBtn.setType(CustomWidgetButton.ButtonType.LeftBtn);
                    backBtn.setTitle(customButton.getButtonText());
//					backBtn.setBeforeImg(context.getResources().getDrawable(
//							R.drawable.return_icon60));
                    backBtn.setBeforeImg(ButtonFactory.getDrawable(context, customButton.getBeforeImg()));
                    backBtn.setAfterImg(ButtonFactory.getDrawable(context, customButton.getAfterImg()));
                    backBtn.setCallBack(customButton.getClickEvent());
                    list.add(backBtn);
                } else if ("operationbtn".equalsIgnoreCase(customButton.getType())) {
                    info = new CustomWidgetButton();
                    info.setBeforeImg(ButtonFactory.getDrawable(context,
                            customButton.getBeforeImg()));
                    info.setAfterImg(ButtonFactory.getDrawable(context, customButton.getAfterImg()));
                    try {
                        info.setNum(Integer.parseInt(customButton.getNumber()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        info.setNum(0);
                    }
                    info.setTitle(customButton.getButtonText());
                    info.setCallBack(customButton.getClickEvent());
                    info.setIsclick(customButton.getIsClick());
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

    public void initializeTabs(final List<CustomButton> tablist, SlideTitle tabWidget) {
        try {
            this.tabWidget = tabWidget;
            Log.e(TAG, "initializeTabs");
            if (tablist == null || tablist.size() == 0) {
                tabWidget.setVisibility(View.GONE);
                return;
            }
            tabWidget.setVisibility(View.VISIBLE);

            List<SlideItem> buttons = new ArrayList<>();
            CustomBtn btn;
            for (CustomButton button : tablist) {
                btn = new CustomBtn();
                btn.setName(button.getButtonText());
                if (button.getIsClick() != null && "true".equals(button.getIsClick())) {
                    btn.setIsCheck(true);
                }
                btn.setEvent(button.getClickEvent());
                buttons.add(btn);
            }
            // 标题点击监听
            tabWidget
                    .setSlideTitleOnClickListener(new SlideTitle.SlideTitleOnClickListener() {
                        @Override
                        public void slideTitleOnClick(SlideItem item) {
                            if (item instanceof CustomBtn) {
                                String callBack = ((CustomBtn) item).getEvent();
                                if (!TextUtils.isEmpty(callBack)) {
                                    loadUrl(callBack);
                                }
                            }

                        }
                    });
            tabWidget.setMidChildTitleFlow(buttons);
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
        WebViewCtrol a = (WebViewCtrol) context;
        a.onJsAlert(view, url, message, result);
        return true;
    }

    @Override
    public void onCloseWindow(WebView window) {
        Log.e("NNN", "onCloseWindow");
        closeCurrentWebView();
        super.onCloseWindow(window);
    }

    public void closeCurrentWebView() {
        try {
            if (webViews.size() > 1) {
                Log.e("NNN", "back to parent:" + webViews.size());
                WebView wb = webViews.pop();
                PullToRefreshWebView pullToRefreshWebView = refreshWebviews.get(wb);
                browserLayout.removeAllViews();
                ViewGroup parent = (ViewGroup) wb.getParent();
                if (parent != null) {
                    parent.removeAllViews();
                }
                browserLayout.addView(pullToRefreshWebView);


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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        refreshWebviews.put(webView, pullRefreshWebView);

        // now create a new web view
//        webView = WebViewFactory.getNewWebView(context, null);
        webView.setWebChromeClient(this);
        webView.setWebViewClient(new RjWebViewClient((WebViewCtrol) context));
        // add the new web view to the layout
//        webView.setLayoutParams(new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        browserLayout.addView(pullRefreshWebView, lp);
        // tell the transport about the new view
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(webView);
        resultMsg.sendToTarget();

        // let's be cool and slide the new web view up into view
//        Animation slideUp = AnimationUtils.loadAnimation(context,
//                R.anim.slide_left);
//        childLayout.startAnimation(slideUp);
        return true;
    }

    /**
     * Lower the child web view down
     */
    public void closeChild() {
        Animation slideDown = AnimationUtils.loadAnimation(context,
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
