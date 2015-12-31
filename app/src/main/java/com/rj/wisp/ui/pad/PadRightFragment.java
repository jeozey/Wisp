package com.rj.wisp.ui.pad;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rj.framework.ButtonFactory;
import com.rj.framework.DB;
import com.rj.framework.webview.RjWebChromeClient;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrolImpl;
import com.rj.view.PadHorizontalBtns;
import com.rj.view.SlideTitle;
import com.rj.view.button.CustomButton;
import com.rj.view.button.CustomWidgetButton;
import com.rj.view.loading.CutsomProgressDialog;
import com.rj.wisp.R;
import com.rj.wisp.core.WISPComponentsParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PadRightFragment extends Fragment {
    public interface RightFragmentListener {
        void onWindowClose(WebView webView, String closeEvent);

        void openFullScreen();

        void closeFullScreen();

        void transHander(Message message);

        void saveHandWriteSuccess(String hwPath);
    }

    private static final String TAG = "PadRightFragment";
    private WebView webView;
    private Activity activity;
    private RightFragmentListener rightFragmentListener;
    private CutsomProgressDialog loadDialog = null;

    public boolean isShowDialog() {
        return (loadDialog != null && loadDialog.isShowing());
    }

    public void showDialogProgress() {
        try {
            Log.e(TAG, "showDialogProgress   loadDialog.isShowing():"
                    + loadDialog.isShowing());
            if (loadDialog != null && !loadDialog.isShowing()) {
                Log.e(TAG, "showDialogProgress");
                loadDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissDialogProgress() {
        try {
            if (loadDialog != null && loadDialog.isShowing()) {
                Log.e(TAG, "dismissDialogProgress");
                loadDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCache(boolean disk) {
        if (webView != null) {
            webView.clearCache(disk);
        }
    }

    // 改变全屏状态
    public void setScreenState(boolean fullScreen, boolean showHandWriteViewShow) {
        Log.e(TAG, "setScreenState:" + fullScreen);
        try {
            isFullScreen = fullScreen;
            webView.requestLayout();
            if (!DB.isDianJian) {
                if (isFullScreen) {
                    screenButton.setBeforeImg(getResources().getDrawable(
                            R.mipmap.ic_back_from_fullscreen));
                    screenButton.setTitle("退出全屏");
                    /*
					 * 为了方便，将此处无用num项作为标志该次是否为全屏的状态：-1表示非全屏，1表示全屏
					 */
                    screenButton.setNum(1);
                } else {
                    screenButton.setBeforeImg(getResources().getDrawable(
                            R.mipmap.ic_back_to_fullscreen));
                    screenButton.setTitle("全屏");
                    screenButton.setNum(-1);
                }
            }
            if (infos != null) {
                padHorizontalBtns.update(screenButton, fontSizeInfo, infos);
            }
            if (!showHandWriteViewShow) {
//				closeHandWriteView();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private RelativeLayout layout;
    private LinearLayout mMainLinearLayout;
    private PopupWindow mPopupWindow;

    private void closePopTablet() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            try {
                mPopupWindow.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static final int SIZE_PADDING = 50;


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

    public void updateButtonBar(String data) {
        try {

            Log.e(TAG, "buttonsJson:" + data);
            // updateBottomButtonBar(buttonsJson);

            Map<String, List> map = WISPComponentsParser
                    .getCustomButtonList4Json(data);

            List<CustomButton> tablist = map.get("tabslist");
            initializeTabs(tablist);

            List<CustomButton> buttonlist = map.get("bottombtnlist");
            ArrayList<CustomWidgetButton> list = new ArrayList<CustomWidgetButton>();
            CustomWidgetButton info;
            CustomWidgetButton operationBtns = new CustomWidgetButton();// 更多按钮
            operationBtns.setBeforeImg(getActivity().getResources()
                    .getDrawable(R.mipmap.ic_more));
            operationBtns.setTitle("更多");
            operationBtns.setPopData(new ArrayList<CustomWidgetButton>());
            for (CustomButton customButton : buttonlist) {
                if ("topOperationbtn".equalsIgnoreCase(customButton.getType())) {
                    info = new CustomWidgetButton();
                    info.setBeforeImg(ButtonFactory.getDrawable(getActivity(),
                            customButton.getBeforeImg()));
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
                } else if ("operationbtn".equalsIgnoreCase(customButton
                        .getType())) {
                    info = new CustomWidgetButton();
                    info.setBeforeImg(ButtonFactory.getDrawable(getActivity(),
                            customButton.getBeforeImg()));
                    try {
                        info.setNum(Integer.parseInt(customButton.getNumber()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        info.setNum(0);
                    }
                    info.setTitle(customButton.getButtonText());
                    info.setCallBack(customButton.getClickEvent());
                    info.setIsclick(customButton.getIsClick());
                    operationBtns.getPopData().add(info);
                } else if ("FontSize".equalsIgnoreCase(customButton.getType())) {
                    fontSizeInfo = new CustomWidgetButton();
                    fontSizeInfo.setBeforeImg(activity.getResources()
                            .getDrawable(R.mipmap.ic_fontsize));
                    fontSizeInfo.setTitle(customButton.getButtonText());

                    List<CustomWidgetButton> popData = new ArrayList<CustomWidgetButton>();
                    info = new CustomWidgetButton();
                    info.setTitle("大");
                    info.setCallBack("javascript:try{ "
                            + customButton.getClickEvent() + "('" + 100
                            + "'); }catch(e){alert(e);}");
                    popData.add(info);

                    info = new CustomWidgetButton();
                    info.setTitle("中");
                    info.setCallBack("javascript:try{ "
                            + customButton.getClickEvent() + "('" + 50
                            + "'); }catch(e){alert(e);}");
                    popData.add(info);

                    info = new CustomWidgetButton();
                    info.setTitle("小");
                    info.setCallBack("javascript:try{ "
                            + customButton.getClickEvent() + "('" + 0
                            + "'); }catch(e){alert(e);}");
                    popData.add(info);
                    fontSizeInfo.setPopData(popData);

                    // list.add(fontSizeInfo);
                } else if ("backbtn".equalsIgnoreCase(customButton.getType())) {
                    closeEvent = customButton.getClickEvent();
                }
            }
            /**
             * @author GuLang
             */
            if (operationBtns.getPopData() != null
                    && operationBtns.getPopData().size() > 0) {
                list.add(operationBtns);
            }

            updateTopButtons(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String closeEvent = null;

    public void initializeTabs(final List<CustomButton> tablist) {
        try {
            // final TopTabLayoutWidget tabWidget = (TopTabLayoutWidget)
            // getView().findViewById(R.id.tabLayoutWidget);
            final SlideTitle tabWidget = (SlideTitle) getView().findViewById(
                    R.id.tabLayoutWidget);
            Log.e(TAG, "tablist:" + tablist);
            // 清空标签栏
            if (tablist == null || tablist.size() == 0) {
                Log.e(TAG, "清空标签栏");
                tabWidget.setVisibility(View.GONE);
                return;
            }

            tabWidget.setVisibility(View.VISIBLE);
            List<CustomWidgetButton> buttons = new ArrayList<CustomWidgetButton>();
            List<String> list = new ArrayList<String>(); // 要显示的标题列表
            CustomWidgetButton b = new CustomWidgetButton();
            for (CustomButton button : tablist) {
                Log.e(TAG, "button:" + button);
                b = new CustomWidgetButton();
                b.setTitle(button.getButtonText());
                list.add(button.getButtonText());
                b.setCallBack(button.getClickEvent());
                b.setIsclick(button.getIsClick());
                buttons.add(b);
            }
            // 获取设备分辨率
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay()
                    .getMetrics(mDisplayMetrics);

            tabWidget.setMidChildTitleFlow(list);

            // 标题点击监听
            tabWidget
                    .setSlideTitleOnClickListener(new SlideTitle.SlideTitleOnClickListener() {
                        @Override
                        public void slideTitleOnClick(int position) {
                            /**
                             * 设置横线显示在哪个子标题下方
                             */
                            // tabWidget.setChangeTitle(position);

                            CustomButton btn = tablist.get(position);

                            String callBack = btn.getClickEvent();
                            if (!TextUtils.isEmpty(callBack)) {
                                loadUrl(callBack);
                            }
                        }
                    });

            int index = -1;
            CustomButton btn = null;
            for (CustomButton button : tablist) {
                Log.e(TAG, "button:" + button);
                if ("true".equals(button.getIsClick())) {
                    btn = button;
                    index++;
                    break;
                }
                index++;
            }
            if (btn != null) {
                tabWidget.setChangeTitle(index);
                String callBack = btn.getClickEvent();
                if (!TextUtils.isEmpty(callBack)) {
                    loadUrl(callBack);
                }
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
            Log.e(TAG, "reload()");
            webView.reload();
        }
    }

    public void loadUrl(String url) {
        if (webView != null) {
            Log.e(TAG, "loadUrl:" + url);
            webView.loadUrl(url);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        System.out.println("RightFragment--->onCreate");
        super.onCreate(savedInstanceState);
    }

    private View parentView;
    private DisplayMetrics dm = new DisplayMetrics();
    private boolean showFullScreen = true;

    public PadRightFragment() {

    }

    public PadRightFragment(Activity activity, WebView webView,
                            View parentView, RightFragmentListener rightFragmentListener,
                            boolean setWebClientflg, boolean showFullScreen) {
        this.activity = activity;
        this.webView = webView;
        this.parentView = parentView;
        this.rightFragmentListener = rightFragmentListener;
        this.showFullScreen = showFullScreen;

        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e(TAG, "DisplayMetrics:" + dm);
        loadDialog = new CutsomProgressDialog(activity, dm.widthPixels,
                parentView);

        if (webView != null && setWebClientflg) {
            RightFragmentWebViewCtrol rightFragmentWebViewCtrol = new RightFragmentWebViewCtrol(
                    activity, parentView);
            webView.setWebViewClient(new RjWebViewClient(
                    rightFragmentWebViewCtrol));
            webView.setWebChromeClient(new RjWebChromeClient(activity,
                    rightFragmentWebViewCtrol));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("RightFragment--->onCreateView");
        return inflater.inflate(R.layout.pad_right_fragment, container, false);
    }

    private RelativeLayout rightRelativeLayout;

    @Override
    public void onResume() {
        if (rightRelativeLayout == null && webView != null) {
            rightRelativeLayout = (RelativeLayout) getView().findViewById(
                    R.id.mainBrowserLayout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            rightRelativeLayout.removeAllViews();
            rightRelativeLayout.addView(webView, layoutParams);
            if (!DB.isDianJian) {
                // 绑定手势事件
                bindWebviewGesture(webView);
            }

            initTopButtons();
        }
        super.onResume();
    }

    private void bindWebviewGesture(WebView webView) {
        final GestureDetector gestureDetector = new GestureDetector(activity,
                new MyGestureListener());
        try {
            if (webView != null) {
                webView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        // if(event.getAction()==MotionEvent.ACTION_UP){
                        // Log.e(TAG, "ACTION_UP");
                        // }else if(event.getAction()==MotionEvent.ACTION_DOWN){
                        // Log.e(TAG, "ACTION_DOWN");
                        // }
                        return gestureDetector.onTouchEvent(event);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    CustomWidgetButton screenButton;
    CustomWidgetButton fontSizeInfo;
    PadHorizontalBtns padHorizontalBtns;
    List<CustomWidgetButton> infos;

    private void updateTopButtons(List<CustomWidgetButton> list) {
        try {
            infos = list;
            if (padHorizontalBtns != null) {
                padHorizontalBtns.update(screenButton, fontSizeInfo, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化顶部按钮
    private void initTopButtons() {
        Log.e(TAG, "initTopButtons");
        if (!DB.isDianJian) {
            screenButton = new CustomWidgetButton();
            screenButton.setBeforeImg(getResources().getDrawable(
                    R.mipmap.ic_back_from_fullscreen));
            screenButton.setTitle("全屏");
            screenButton.setCallBack("fullScreen");
            Log.e(TAG, "showFullScreen:" + showFullScreen);
            if (!showFullScreen) {
                screenButton.setVisiable(View.INVISIBLE);
            }
        }

        // infos = getListData();

        padHorizontalBtns = (PadHorizontalBtns) getView().findViewById(
                R.id.horizontal_btn);
        MyCallBack callBack = new MyCallBack();
        padHorizontalBtns.init(dm.widthPixels, screenButton,
                new ArrayList<CustomWidgetButton>(), null, callBack);
    }

    private boolean isFullScreen = false;

    class MyCallBack implements PadHorizontalBtns.HorizontalBtnsCallBack {
        @Override
        public void callBack(int type, CustomWidgetButton info, int popPosition) {
            switch (type) {
                case PadHorizontalBtns.HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_SCREEN:
                    Log.i(TAG, "屏幕操作");
                    if (screenButton != null) {
                        if ("全屏".equals(screenButton.getTitle())) {
                            // 此时是非全屏，需要显示全屏
                            rightFragmentListener.openFullScreen();
                            setScreenState(true, false);
                            isFullScreen = true;
                        } else {
                            // 需要显示全屏
                            rightFragmentListener.closeFullScreen();
                            setScreenState(false, false);
                            isFullScreen = false;
                        }
                    }

                    break;
                case PadHorizontalBtns.HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_COMMON_ITEM:
                    Log.i(TAG, "普通回调");
                    if (!TextUtils.isEmpty(info.getCallBack())) {
                        loadUrl(info.getCallBack());
                    }
                    break;
                case PadHorizontalBtns.HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_POP_ITEM:
                    Log.i(TAG, "弹出回调");
                    Toast.makeText(
                            activity,
                            "横向listview有PopWindow项-->" + info + " PopWindow中选中第"
                                    + popPosition + "项", Toast.LENGTH_SHORT).show();
                    break;
                case PadHorizontalBtns.HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_FONTSIZE:
                    Log.i(TAG, "字号回调");
                    Toast.makeText(activity, "字体 选中:", Toast.LENGTH_SHORT).show();
                    break;
                case PadHorizontalBtns.HorizontalBtnsCallBack.CALL_BACK_MSG_TYPE_CLOSE:
                    Log.i(TAG, "关闭回调");
                    try {
                        rightFragmentListener.onWindowClose(webView, closeEvent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    class RightFragmentWebViewCtrol extends WebViewCtrolImpl {

        public RightFragmentWebViewCtrol(Activity activity, View parentView) {
            super(activity);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.v("request", "onPageStarted:" + url);
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
                loadDialog.dismiss();
            } catch (Exception e) {
            }
        }

        @Override
        public void onCreateWindow(WebView webview, boolean isUserGesture,
                                   Message resultMsg) {
            Log.e(TAG, "onCreateWindows0");
            if (resultMsg == null) {
                return;
            }
            Log.e(TAG, "onCreateWindows1");
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;

            transport.setWebView(webview);
            resultMsg.sendToTarget();

            // 这边的window.open暂时用这种方法实现
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // by jeozey 不能remove掉 因为页面的window.opener会找不到
            // rightRelativeLayout.removeAllViews();
            webView = webview;
            rightRelativeLayout.addView(webView, layoutParams);
            // 绑定手势事件
            bindWebviewGesture(webView);
            Log.e(TAG, "onCreateWindows2");
            Log.e(TAG, "webview.getUrl():" + webView.getUrl());
            Log.e(TAG, "onCreateWindows3");
            super.onCreateWindow(webview, isUserGesture, resultMsg);
        }

        @Override
        public void onCloseWindow(WebView webView) {
            if (rightFragmentListener != null) {
                rightFragmentListener.onWindowClose(webView, closeEvent);
            }
            super.onCloseWindow(webView);
        }
    }

    /*******************
     * 手势
     ************************/

    class MyGestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            Log.e(TAG, "onFling");
            if (e1.getX() - e2.getX() > 300) {
                Log.e(TAG, "left");
                if (!isFullScreen) {
                    setScreenState(true, false);
                    rightFragmentListener.openFullScreen();
                }
                return true;
            } else if (e1.getX() - e2.getX() < -300) {
                Log.e(TAG, "right");
                if (isFullScreen) {
                    setScreenState(false, false);
                    rightFragmentListener.closeFullScreen();
                }
                return true;
            }
            return false;
        }
    }

}