package com.rj.wisp.ui.pad;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rj.framework.ButtonFactory;
import com.rj.framework.DB;
import com.rj.framework.download.DownloadDialog;
import com.rj.framework.download.DownloadDialog.CancleDownLoad;
import com.rj.framework.download.DownloadDialogHandler;
import com.rj.framework.download.DownloadStates;
import com.rj.framework.webview.RjWebChromeClient;
import com.rj.framework.webview.RjWebViewClient;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.framework.webview.WebViewCtrolImpl;
import com.rj.framework.webview.WebViewFactory;
import com.rj.view.MenuButtonsLayout;
import com.rj.view.MoveLayout;
import com.rj.view.MoveLayoutItemInfo;
import com.rj.view.PopMenu;
import com.rj.view.TabMenu;
import com.rj.view.ToastTool;
import com.rj.view.button.ButtonNum;
import com.rj.view.button.CustomButton;
import com.rj.view.button.CustomWidgetButton;
import com.rj.view.loading.CutsomProgressDialog;
import com.rj.wisp.R;
import com.rj.wisp.UploadDialogHandler;
import com.rj.wisp.activity.LoginActivity;
import com.rj.wisp.bean.Attachment;
import com.rj.wisp.bean.AttachmentDownEvent;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.core.AttachmentCacheUtil;
import com.rj.wisp.core.Commons;
import com.rj.wisp.core.FileOpenUtil;
import com.rj.wisp.core.WISPComponentsParser;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.widget.AppSettingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


public class PadMainActivity extends FragmentActivity implements
        PadLeftFragment.LeftFragmentListener, OnClickListener {
    private static final String TAG = PadMainActivity.class.getName();

    private ProgressDialog downAttachmentProgress;
    private LinearLayout mainLinearLayout;
    private FrameLayout leftLayout, rightLayout;
    private DownloadDialogHandler downloadDialogHandler;
    private UploadDialogHandler uploadDialogHandler;
    private PadLeftFragment leftFragment;
    private PadLeftFragment homeFragment;
    private PadRightFragment rightFragment, poPFragment;
    // private PadRightFragment popFragment;
//	private HandWriting handWriting;
    private CutsomProgressDialog loadDialog = null;
    private String logoutEvent = "";
    private boolean cancleDownLoad = false;// 取消本次下载
    private boolean loginIn = true;// 是否登陆
    private String popHomePageUrl;
    private WebViewCtrol webViewCtrol;

    // 判断右侧是否关闭
    private boolean isRightFragmentOpen() {
        Log.e(TAG, "isRightFragmentOpen:"
                + (rightLayout.getVisibility() == View.VISIBLE));
        return rightLayout.getVisibility() == View.VISIBLE ? true : false;
    }

    // 判断首页是否关闭
    private boolean isHomeFragmentOpen() {
        View v = findViewById(R.id.home_frame_layout);
        Log.e(TAG, "isHomeFragmentOpen:" + (v.getVisibility() == View.VISIBLE));
        return v.getVisibility() == View.VISIBLE ? true : false;
    }

    // 判断PopFragment是否关闭
    private boolean isPopFragmentOpen() {
        View v = findViewById(R.id.pop_frame_layout);
        Log.e(TAG, "isPopFragmentOpen:" + (v.getVisibility() == View.VISIBLE));
        return v.getVisibility() == View.VISIBLE ? true : false;
    }

    private void goBack() {
        if (isPopFragmentOpen()) {
            poPFragment.goBack();
        } else if (isHomeFragmentOpen()) {
            homeFragment.goBack();
        } else if (isRightFragmentOpen()) {
            rightFragment.goBack();
        } else {
            leftFragment.goBack();
        }
    }

    private void reload() {
        if (isPopFragmentOpen()) {
            Log.e(TAG, "popWebView.reload()");
            poPFragment.reload();
        } else if (isHomeFragmentOpen()) {
            Log.e(TAG, "homeWebView.reload()");
            homeFragment.reload();
        } else if (isRightFragmentOpen()) {
            Log.e(TAG, "rightFragment.reload()");
            rightFragment.reload();
        } else {
            if (leftFragment != null) {
                Log.e(TAG, "leftFragment.reload()");
                leftFragment.reload();
            }
        }

    }

    private void loadUrl(String url) {
        try {
            if (isPopFragmentOpen()) {
                poPFragment.loadUrl(url);
            } else if (isHomeFragmentOpen()) {
                homeFragment.loadUrl(url);
            } else if (isRightFragmentOpen()) {
                rightFragment.loadUrl(url);
            } else {
                leftFragment.loadUrl(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializeTabs(List<CustomButton> tablist) {
        try {
            if (rightFragment != null) {
                rightFragment.initializeTabs(tablist);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void openRightFullScreen(final boolean showHandWriteViewShow) {
        try {
            if (leftLayout != null && leftLayout.getVisibility() != View.GONE) {

                Animation lefSslideLeft = AnimationUtils.loadAnimation(
                        getBaseContext(), R.anim.pad_left_hide);
                leftLayout.startAnimation(lefSslideLeft);
                lefSslideLeft.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        leftLayout.setVisibility(View.GONE);
                        if (rightFragment != null) {
                            rightFragment.setScreenState(true,
                                    showHandWriteViewShow);
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void showLeftView() {
        try {
            if (leftLayout.getVisibility() == View.GONE) {
                Animation lefSslideLeft = AnimationUtils.loadAnimation(
                        getBaseContext(), R.anim.pad_left_show);
                leftLayout.startAnimation(lefSslideLeft);
                lefSslideLeft.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        leftLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isProgressDialogShowing() {
        return ((loadDialog != null && loadDialog.isShowDialog) || ((rightFragment != null && rightFragment
                .isShowDialog())));
    }

    @Override
    public void showSettingView() {
        new AppSettingDialog(PadMainActivity.this).show();
    }

    @Override
    public void showUserInfo() {
        ToastTool.show(PadMainActivity.this, "用户信息", Toast.LENGTH_SHORT);
    }

    @Override
    public void logOut(String event) {
        if (!TextUtils.isEmpty(event)) {
            leftFragment.loadUrl(event);
        } else {
            showToast("没有获取到注销事件");
        }
    }

    @Override
    public void setLogOutEvent(String logOutEvent) {
        logoutEvent = logOutEvent;
    }

    @Override
    public void onWindowOpen(WebView webView) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Log.e(TAG, "USER-AGENT:" + webView.getSettings().getUserAgentString());
        if (isHomeFragmentOpen()) {
            Log.e(TAG, "isHomeFragmentOpen yes");
            showPopView(webView);
            transaction.replace(R.id.pop_frame_layout, poPFragment);
        } else {
            Log.e(TAG, "isHomeFragmentOpen not");
            showRightView(webView);
            transaction.replace(R.id.right_fragment_layout, rightFragment);
        }

        transaction.addToBackStack(null);
        // 提交修改
        transaction.commit();
    }

    private void showPopView(WebView webView) {
        if (popFrameLayout == null) {
            popFrameLayout = (FrameLayout) findViewById(R.id.pop_frame_layout);
        }
        poPFragment = new PadRightFragment(PadMainActivity.this, webView,
                popFrameLayout, new PadRightFragment.RightFragmentListener() {
            @Override
            public void transHander(Message message) {
                handler.sendMessage(message);
            }

            @Override
            public void onWindowClose(WebView webView, String closeEvent) {
                Log.e(TAG, "onWindowClose:" + closeEvent);
                if (TextUtils.isEmpty(closeEvent)) {
                    if (isPopFragmentOpen()) {
                        poPFragment.loadUrl(closeEvent);
                    } else {
                        rightFragment.loadUrl(closeEvent);
                    }
                }
                if (isPopFragmentOpen()) {
                    findViewById(R.id.pop_frame_layout).setVisibility(
                            View.GONE);
                } else {
                    closeRightView();
                }

            }

            @Override
            public void openFullScreen() {
                openRightFullScreen(false);

            }

            @Override
            public void closeFullScreen() {
                Log.e(TAG, "closeFullScreen");
                showLeftView();
            }

            @Override
            public void saveHandWriteSuccess(String hwPath) {
                lastDownLoadHwPath = hwPath;
                Log.i("wanan",
                        "saveHandWriteSuccess lastDownLoadHwUrl="
                                + lastDownLoadHwPath);
            }
        }, true, false);

        // isPopFragmentOpen = true;

        findViewById(R.id.pop_frame_layout).setOnTouchListener(
                new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // 上下层布局点击事件不能让他传递下去
                        Log.e(TAG, "onTouch");
                        return true;
                    }
                });

        findViewById(R.id.pop_frame_layout).setVisibility(View.VISIBLE);

    }

    private void showRightView(WebView webView) {
        rightFragment = new PadRightFragment(PadMainActivity.this, webView,
                rightLayout, new PadRightFragment.RightFragmentListener() {
            @Override
            public void transHander(Message message) {
                handler.sendMessage(message);
            }

            @Override
            public void onWindowClose(WebView webView, String closeEvent) {
                Log.e(TAG, "onWindowClose:" + closeEvent);
                if (TextUtils.isEmpty(closeEvent)) {
                    rightFragment.loadUrl(closeEvent);
                }
                closeRightView();
            }

            @Override
            public void openFullScreen() {
                openRightFullScreen(false);

            }

            @Override
            public void closeFullScreen() {
                Log.e(TAG, "closeFullScreen");
                showLeftView();
            }

            @Override
            public void saveHandWriteSuccess(String hwPath) {
                lastDownLoadHwPath = hwPath;
                Log.i("wanan",
                        "saveHandWriteSuccess lastDownLoadHwUrl="
                                + lastDownLoadHwPath);
            }
        }, true, true);

        rightLayout.setVisibility(View.VISIBLE);
        Animation rightSlideLeft = AnimationUtils.loadAnimation(
                getBaseContext(), R.anim.pad_right_show);
        rightLayout.startAnimation(rightSlideLeft);
        // isRightViewOpen = true;
        rightSlideLeft.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
//				blankView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }
        });
    }

    private void closeRightView() {
        try {
            if (rightLayout.getVisibility() == View.VISIBLE) {

                if (leftLayout.getVisibility() == View.VISIBLE) {
                    Animation rightSlideLeft = AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pad_right_hide);
                    rightLayout.startAnimation(rightSlideLeft);
                    rightSlideLeft
                            .setAnimationListener(new AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationRepeat(
                                        Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rightFragment.setScreenState(false, false);
                                    rightLayout.setVisibility(View.GONE);
                                    // blankView.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    Animation lefSslideLeft = AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pad_left_show);
                    leftLayout.startAnimation(lefSslideLeft);
                    lefSslideLeft.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            leftLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }
                    });

                    Animation rightSlideLeft = AnimationUtils.loadAnimation(
                            getBaseContext(), R.anim.pad_right_hide_full);
                    rightLayout.startAnimation(rightSlideLeft);
                    rightSlideLeft
                            .setAnimationListener(new AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationRepeat(
                                        Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    rightLayout.setVisibility(View.GONE);
                                    // blankView.setVisibility(View.VISIBLE);
                                }
                            });
                }
            }
            // isRightViewOpen = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEventMainThread(AttachmentDownEvent event) {
        Log.e(TAG, "onEvent AttachmentDownEvent:" + event);
        if (event != null) {
            switch (event.getDownResult()) {
                case Commons.ATTACHMENT_DOWN_SUCC:
                    handleAttachmentDowning(event);
                    break;
                case Commons.ATTACHMENT_DOWN_FAIL:
                case Commons.ATTACHMENT_DOWN_COMPLETE:
                case Commons.ATTACHMENT_DOWN_CACHE:
                    handleAttachmentDownOver(event);
                    break;
            }
        }
    }


    private Object attachmentObj = new Object();

    private void handleAttachmentDowning(AttachmentDownEvent event) {
        synchronized (attachmentObj) {
            String downUrl = event.getDownUrl();
            if (!attachmentDownEvents.containsKey(downUrl)) {
                attachmentDownEvents.put(downUrl, event);
                downAttachmentProgress.setMax(event.getFileLength());
                downAttachmentProgress.show();
            }
            downAttachmentProgress.setProgress(downAttachmentProgress.getProgress() + event.getHasDownLength());
        }
    }

    private Map<String, AttachmentDownEvent> attachmentDownEvents;

    private void handleAttachmentDownOver(AttachmentDownEvent event) {
        String downUrl = event.getDownUrl();
        if (Commons.ATTACHMENT_DOWN_CACHE == event.getDownResult()) {
            Log.e(TAG, "ATTACHMENT_DOWN_CACHE:" + event);
            ToastTool.show(PadMainActivity.this, "已经下载过了", Toast.LENGTH_LONG);

            Attachment attachment = AttachmentCacheUtil.getAttachment(downUrl);
            Intent intent = FileOpenUtil.openFile(attachment, getBaseContext());
            startActivity(intent);
        } else if (attachmentDownEvents.containsKey(downUrl)) {
            AttachmentDownEvent e = attachmentDownEvents.get(downUrl);
            Attachment attachment = new Attachment(downUrl, e.getPath(), e.getContentType(), e.getFileLength());
            AttachmentCacheUtil.putAttachment(attachment);

            attachmentDownEvents.remove(downUrl);
            downAttachmentProgress.dismiss();

            if (Commons.ATTACHMENT_DOWN_COMPLETE == event.getDownResult()) {
                ToastTool.show(PadMainActivity.this, "下载完毕", Toast.LENGTH_LONG);
            } else {
                ToastTool.show(PadMainActivity.this, "" + event.getDownFailMsg(), Toast.LENGTH_LONG);
            }
            try {
                Intent intent = FileOpenUtil.openFile(attachment, getBaseContext());
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                ex.printStackTrace();
                String contentType = e.getContentType();
                ToastTool.show(PadMainActivity.this, "没有合适的软件打开该附件 " + (contentType != null ? contentType : ""), Toast.LENGTH_SHORT);
            }
        }

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Log.e(TAG, "msg.what:" + msg.what);
                switch (msg.what) {
                    case HandlerWhat.LOGIN_PAGE:
                        leftMenuButtonsLayout.setVisibility(View.GONE);
                        titleLayout.setVisibility(View.GONE);
                        break;
                    case HandlerWhat.LOG_OUT:
                        logOut(logoutEvent);
                        break;
                    case HandlerWhat.ADD_WEB_UI:
                        addWebUI(msg);
                        break;
                    case HandlerWhat.ADD_WEB_BTN_NUM:
                        addWebBtnNum(msg);
                        break;
                    case HandlerWhat.SHOW_TOAST:
                        if (msg.obj != null) {
                            showToast(msg.obj.toString());
                        }
                        break;
                    case HandlerWhat.SHOW_DIALOG:
                        if (msg.obj != null) {
                            showDialog(msg);
                        }
                        break;
                    case HandlerWhat.SHOW_LOADING:
                        showProgressDialog();
                        break;
                    case HandlerWhat.DISMISS_LOADING:
                        dismissProgressDialog();
                        break;
                    case HandlerWhat.HANDWRITING_OPEN:
                        dismissProgressDialog();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    private void showDialog(Message msg) {
        if (msg.obj != null) {
            try {
                String type = msg.getData().getString("type");
                String content = msg.getData().getString("text");
                String title = msg.getData().getString("title");
                final String callBack = msg.getData().getString("callBack");

                AlertDialog.Builder customBuilder = new AlertDialog.Builder(PadMainActivity.this).setTitle(title).setMessage(content);
                customBuilder.setTitle(title).setMessage(content)
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                showConform(callBack, "false");
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                showConform(callBack, "true");
                            }
                        });
                AlertDialog dialog = customBuilder.create();

                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void showConform(String callBack, String r) {
        if (callBack.indexOf("()") != -1) {
            callBack = callBack.replace("()", "(" + r + ")");
        } else if (callBack.indexOf("(") != -1) {
            callBack = callBack.replace(")", "," + r + ")");
        } else {
            callBack = callBack + "(" + r + ")";
        }

        loadUrl("javascript:try{ "
                + callBack
                + "; }catch(e){alert(e);}");
    }

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            ToastTool.show(PadMainActivity.this, msg, Toast.LENGTH_SHORT);
        }
    }

    // 更新条数
    private void addWebBtnNum(Message msg) {

        try {
            ButtonNum btnNum = (ButtonNum) msg.obj;
            CustomWidgetButton button;
            button = new CustomWidgetButton();
            button.setTitle(btnNum.getButtonText());
            button.setNum(Integer.valueOf(btnNum.getNumber()));
            leftMenuButtonsLayout.updateBtn(button);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWebUI(Message msg) {
        try {
            if (titleLayout.getVisibility() == View.GONE) {
                titleLayout.setVisibility(View.VISIBLE);
            }
            if (leftMenuButtonsLayout.getVisibility() == View.GONE) {
                leftMenuButtonsLayout.setVisibility(View.VISIBLE);
            }
            if (msg.obj != null) {
                if (isPopFragmentOpen()) {
                    if (poPFragment != null)
                        poPFragment.updateButtonBar(msg.obj.toString());
                } else if (isRightFragmentOpen()) {
                    if (rightFragment != null)
                        rightFragment.updateButtonBar(msg.obj.toString());
                } else {
                    updateButtonBar(msg.obj.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastTool.show(getBaseContext(), "界面组件更新出错",
                    Toast.LENGTH_SHORT);
        }
    }

    private boolean isMoreLayoutShow = false;

    private void showMoreLayout(String url) {
        try {
            if (!isMoreLayoutShow) {
                if (moreWebView != null)
                    moreWebView.loadUrl(url);

                Animation slideUp = AnimationUtils.loadAnimation(
                        PadMainActivity.this, R.anim.pad_more_up_show);
                moreRelativeLayout.setVisibility(View.VISIBLE);
                moreRelativeLayout.startAnimation(slideUp);
                isMoreLayoutShow = true;
            } else {
                hideMoreLayout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideMoreLayout() {
        try {
            if (isMoreLayoutShow) {
                Animation slideUp = AnimationUtils.loadAnimation(
                        PadMainActivity.this, R.anim.pad_more_down_hide);

                moreRelativeLayout.startAnimation(slideUp);
                slideUp.setAnimationListener(new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        moreRelativeLayout.setVisibility(View.GONE);

                    }
                });
                isMoreLayoutShow = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WebView moreWebView;
    private MoveLayout moveLayout;

    // 更多按钮
    private void initMoreButtons(List<MoveLayoutItemInfo> list) {
        try {
            if (list == null || list.size() == 0) {
                moveLayout.clear();
                return;
            }
            moveLayout.init(list, new MoveLayout.MoveLayoutCallBack() {
                @Override
                public void moveLayoutCallBack(MoveLayoutItemInfo info) {// 更多按钮回调
                    closeRightView();

                    Animation slideDown = AnimationUtils.loadAnimation(
                            PadMainActivity.this, R.anim.pad_more_down_hide);
                    moreRelativeLayout.startAnimation(slideDown);
                    slideDown
                            .setAnimationListener(new AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    moreRelativeLayout
                                            .setVisibility(View.INVISIBLE);
                                    isMoreLayoutShow = false;
                                }

                                @Override
                                public void onAnimationRepeat(
                                        Animation animation) {

                                }
                            });
                    leftFragment.loadUrl(info.getCallBack());
                    // titleTextView.setText(info.getTitle());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RelativeLayout titleLayout;
    private MenuButtonsLayout leftMenuButtonsLayout;
    private List<MoveLayoutItemInfo> moreButtons;
    private RelativeLayout moreRelativeLayout;
    private List<CustomButton> collectionlist;
    private PadLeftFragment.LeftFragmentWebViewCtrol leftFragmentWebViewCtrol;

    // 更新条数
    public void updateButtonNumAsynchronous(String type, String buttonText,
                                            String number) {
        try {
            CustomWidgetButton button;
            button = new CustomWidgetButton();
            button.setTitle(buttonText);
            button.setNum(Integer.valueOf(number));

            leftMenuButtonsLayout.updateBtn(button);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateButtonBar(String data) {
        try {
            Log.e(TAG, "updateButtonBar buttonsJson:" + data);
            if (data.indexOf("menubtn") != -1) {

                Map<String, List> map = WISPComponentsParser
                        .getCustomButtonList4Json(data);

                collectionlist = map.get("collectionlist");

                // 左侧按钮
                ArrayList<CustomWidgetButton> list = new ArrayList<CustomWidgetButton>();
                moreButtons = new ArrayList<MoveLayoutItemInfo>();
                CustomWidgetButton moreBtn = null;
                for (CustomButton btn : collectionlist) {
                    Log.e(TAG, "collectionlist-pbtn:" + btn.getButtonText());
                    moreBtn = new CustomWidgetButton();
                    moreBtn.setBeforeImg(ButtonFactory.getDrawable(
                            PadMainActivity.this, btn.getBeforeImg()));
                    try {
                        moreBtn.setNum(Integer.parseInt(btn.getNumber()));
                    } catch (Exception e) {
                        // e.printStackTrace();
                        moreBtn.setNum(0);
                    }
                    moreBtn.setTitle(btn.getButtonText());
                    moreBtn.setCallBack(btn.getClickEvent());
                    List<CustomButton> appsrowlist = btn.getCollction();
                    for (CustomButton pButton : appsrowlist) {
                        Log.e(TAG,
                                "collectionlist-sbtn:"
                                        + pButton.getButtonText());
                        MoveLayoutItemInfo parent = new MoveLayoutItemInfo();
                        parent.setDrawable(ButtonFactory.getDrawable(
                                PadMainActivity.this, pButton.getBeforeImg()));
                        try {
                            parent.setNum(Integer.parseInt(pButton.getNumber()));
                        } catch (Exception e) {
                            parent.setNum(0);
                        }
                        parent.setTitle(pButton.getButtonText());
                        parent.setCallBack(pButton.getClickEvent());
                        parent.setType(1);
                        moreButtons.add(parent);

                        for (CustomButton sButton : pButton.getCollction()) {
                            Log.e(TAG,
                                    "collectionlist-sbtn:"
                                            + sButton.getButtonText());
                            MoveLayoutItemInfo son = new MoveLayoutItemInfo();
                            son.setDrawable(ButtonFactory.getDrawable(
                                    PadMainActivity.this,
                                    sButton.getBeforeImg()));
                            try {
                                son.setNum(Integer.parseInt(sButton.getNumber()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                son.setNum(2);
                            }
                            // son.setNum(2);
                            son.setTitle(sButton.getButtonText());
                            son.setCallBack(sButton.getClickEvent());
                            son.setType(2);
                            moreButtons.add(son);
                        }
                    }
                }
                List<CustomButton> buttonlist = map.get("bottombtnlist");


                initMoreButtons(moreButtons);

                leftMenuButtonsLayout = (MenuButtonsLayout) findViewById(R.id.menu_buttons_lyt);
                CustomWidgetButton info;
                for (CustomButton customButton : buttonlist) {
                    if ("menubtn".equalsIgnoreCase(customButton.getType())) {
                        info = new CustomWidgetButton();
                        info.setBeforeImg(ButtonFactory.getDrawable(
                                PadMainActivity.this,
                                customButton.getBeforeImg()));
                        try {
                            info.setNum(Integer.parseInt(customButton
                                    .getNumber()));
                        } catch (Exception e) {
                            e.printStackTrace();
                            info.setNum(0);
                        }
                        info.setTitle(customButton.getButtonText());
                        info.setCallBack(customButton.getClickEvent());
                        info.setIsclick(customButton.getIsClick());
                        list.add(info);
                        // } else if
                        // ("title".equalsIgnoreCase(customButton.getType())) {
                        // String user = customButton.getButtonText();
                        // titleTextView.setText(user);
                    } else if ("logout"
                            .equalsIgnoreCase(customButton.getType())) {
                        logoutEvent = customButton.getClickEvent();
                        Log.e(TAG, "logoutEvent:" + logoutEvent);
                        PadMainActivity.this
                                .setLogOutEvent(logoutEvent);// 设置注销事件
                    }
                }
                if (moreBtn != null) {
                    list.add(moreBtn);
                }

                leftMenuButtonsLayout.init(list, new MenuButtonsLayout.MenuButtonLayoutCallBack() {
                    @Override
                    public void onClickCallBack(int type,
                                                final CustomWidgetButton result) {
                        switch (type) {
                            case MenuButtonsLayout.MenuButtonLayoutCallBack.TYPE_LIST_ITEM:
                                if (result != null) {
                                    closeRightView();
                                    Log.i(TAG, "回调被调用了:" + result.getCallBack());
                                    if ("更多".equals(result.getTitle())) {
                                        if (result.getCallBack() != null
                                                && result.getCallBack().indexOf(
                                                "moreUrl") != -1) {
                                            if (moreWebView == null) {
                                                moreWebView = WebViewFactory
                                                        .getNewWebView(
                                                                PadMainActivity.this,
                                                                null);
                                                // moreWebView =
                                                // (WebView)findViewById(R.id.more_webview);
                                                moreWebView
                                                        .setWebViewClient(new RjWebViewClient(
                                                                leftFragmentWebViewCtrol));
                                                moreWebView
                                                        .setWebChromeClient(new RjWebChromeClient(
                                                                PadMainActivity.this,
                                                                leftFragmentWebViewCtrol));
                                                moreRelativeLayout
                                                        .addView(
                                                                moreWebView,
                                                                new LayoutParams(
                                                                        LayoutParams.MATCH_PARENT,
                                                                        LayoutParams.MATCH_PARENT));

                                                moveLayout.setVisibility(View.GONE);
                                            }
                                            String url = result.getCallBack()
                                                    .replace("moreUrl=", "");
                                            url = (DB.PRE_URL + url.replace(
                                                    "adapter?open&url=", ""));
                                            showMoreLayout(url);
                                        } else {
                                            showMoreLayout(null);
                                        }
                                    } else if (!TextUtils.isEmpty(result
                                            .getCallBack())) {
                                        if (isMoreLayoutShow) {
                                            // 隐藏更多按钮界面
                                            hideMoreLayout();
                                        }
                                        Log.e(TAG,
                                                "result.getCallBack():"
                                                        + result.getCallBack());
                                        leftFragment.loadUrl(result.getCallBack());
                                        // titleTextView.setText(result.getTitle());
                                    }
                                }
                                break;
                            case MenuButtonsLayout.MenuButtonLayoutCallBack.TYPE_SETTING:
                                showSettingView();
                                break;
                            case MenuButtonsLayout.MenuButtonLayoutCallBack.TYPE_LOGOUT:
                                logOut(logoutEvent);
                                break;

                            default:
                                break;
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PopupWindow popupWindow;

    public void closeRJPopupWindows() {
        try {
            Log.e(TAG, "closeRJPopupWindows");
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
            // closeNDRCurl = "";
            // if (TestNEWopen.webView != null) {
            // HdAppLoadActivity.hdAppLoadActivity.popMap
            // .remove((String) TestNEWopen.webView.getTag());
            // // TestNEWopen.webView.destroy();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String QCodeCallBack = "";
    private static final int CAPTURE_ACTIVITY = 15;


    private void dismissDownLoadDialog() {
        Log.e(TAG, "dismissDownLoadDialog");
        Message msg5 = Message.obtain();
        msg5.obj = DownloadStates.MESSAGE_DOWNLOAD_COMPLETE;
        downloadDialogHandler.sendMessage(msg5);
    }

    private void dismissUpLoadDialog() {
        Message msg5 = Message.obtain();
//        msg5.obj = UploadStates.MESSAGE_UPLOAD_COMPLETE;
//        uploadDialogHandler.sendMessage(msg5);
    }

    private void showProgressDialog() {
        try {
            if (loadDialog == null) {
                loadDialog = new CutsomProgressDialog(PadMainActivity.this,
                        dm.widthPixels, leftLayout);
            }
            Log.e(TAG, "loadDialog:" + loadDialog);
            Log.e(TAG, "showProgressDialog loadDialog.isShowing():"
                    + loadDialog.isShowing());
            if (isPopFragmentOpen()) {
                poPFragment.showDialogProgress();
            } else if (isHomeFragmentOpen()) {
                homeFragment.showLoading();
            } else if (isRightFragmentOpen()) {
                rightFragment.showDialogProgress();
            } else if (loadDialog != null && !loadDialog.isShowing()) {
                Log.e(TAG, "showProgressDialog");
                loadDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        try {
            if (isPopFragmentOpen()) {
                poPFragment.dismissDialogProgress();
            } else if (isHomeFragmentOpen()) {
                homeFragment.dismissLoading();
            } else if (isRightFragmentOpen()) {
                rightFragment.dismissDialogProgress();
            } else if (loadDialog != null && loadDialog.isShowing()) {
                loadDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    private ScreenOnReceiver screenOnReceiver;
    //	private View blankView;
    private DisplayMetrics dm;
    private PopMenu settingPopMenu;

    /**
     * Called when the PadMainActivity.this is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.pad_main);

        try {
            WispCore.getWISPSO().StartService(handler, getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();


        initOther();

    }

    private void initView() {

        settingPopMenu = new PopMenu(PadMainActivity.this, handler,
                new PopMenu.PopMenuOnItemClickListener() {

                    @Override
                    public void onItemClick(int index) {
                        switch (index) {
                            case 0:
                                logOut(logoutEvent);
                                break;
                            case 1:
                                reset();
                                break;
                            case 2:
                                showSettingView();
                                break;

                            default:
                                break;
                        }
                    }
                });

        titleLayout = (RelativeLayout) findViewById(R.id.titleLayout);

        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);

        leftMenuButtonsLayout = (MenuButtonsLayout) findViewById(
                R.id.menu_buttons_lyt);

        moreRelativeLayout = (RelativeLayout) findViewById(
                R.id.pad_more_layout);
        moveLayout = (MoveLayout) findViewById(R.id.move_lyt);

        leftFragmentWebViewCtrol = new PadLeftFragment().new LeftFragmentWebViewCtrol(
                PadMainActivity.this, leftLayout);

        popHomePageUrl = getIntent().getStringExtra("popHomePageUrl");
        final FrameLayout homeMainLayout = (FrameLayout) findViewById(R.id.home_frame_layout);
        if (!TextUtils.isEmpty(popHomePageUrl)) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();

            homeFragment = new PadLeftFragment(homeMainLayout, "", true);
            transaction.add(R.id.home_frame_layout, homeFragment,
                    "homeFragment");
            transaction.commit();

            homeFragment.showLoading();
            homeMainLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initOther() {
        attachmentDownEvents = new HashMap<>();
        downAttachmentProgress = new ProgressDialog(PadMainActivity.this);
        downAttachmentProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        downAttachmentProgress.setMessage(getResources().getString(R.string.downloading));
        downAttachmentProgress.setCancelable(false);
//        downAttachmentProgress.setOnKeyListener(onKeyListener);
        tabMenu = new TabMenu(this, new BodyClickEvent(), 0);// 出现与消失的动画
        tabMenu.update();
        tabMenu.SetBodyAdapter(bodyAdapter);

        leftLayout = (FrameLayout) findViewById(R.id.left_fragment_layout);
        rightLayout = (FrameLayout) findViewById(R.id.right_fragment_layout);
        // rightLayout.setVisibility(View.INVISIBLE);

        downloadDialogHandler = new DownloadDialogHandler(new DownloadDialog(
                PadMainActivity.this, new CancleDownLoad() {

            @Override
            public void cancle() {
                // 返回键取消下载
                if (downloadDialogHandler != null) {
                    dismissDownLoadDialog();
                    cancleDownLoad = true;
                }
            }
        }));

        findViewById(R.id.settingBtn).setOnClickListener(this);
        findViewById(R.id.userBtn).setOnClickListener(this);


        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        // 动态增加Fragment
        leftFragment = new PadLeftFragment(leftLayout, DB.PRE_URL
                + DB.LOGINPAGE_URL, false);
        transaction
                .add(R.id.left_fragment_layout, leftFragment, "leftfragment");
        transaction.commit();
    }

    private FrameLayout popFrameLayout;


    private void showPopWebView(final String url, final float percent) {
        mainLinearLayout.post(new Runnable() {

            @Override
            public void run() {

                Log.e(TAG, "popHomePageUrl:" + popHomePageUrl);

                LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = mLayoutInflater.inflate(R.layout.rjpopupwindownew,
                        null);
                view.setFocusableInTouchMode(true);// 能够获得焦点
                view.setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            switch (keyCode) {
                                case KeyEvent.KEYCODE_BACK:
                                    // checkAndClear();
                                    break;
                                case KeyEvent.KEYCODE_MENU:
                                    // checkAndClear();
                                    break;
                            }
                        }
                        return true;
                    }
                });

                popupWindow = new PopupWindow(view,
                        (int) (dm.widthPixels * percent),
                        (int) (dm.heightPixels * percent), true);
                // http://www.526net.com/blog/mobile/80.html
                popupWindow.setBackgroundDrawable(new BitmapDrawable());

                WebView popWebView = (WebView) popupWindow.getContentView()
                        .findViewById(R.id.popWebView);
                popWebView.setInitialScale(100);
                popWebView.getSettings().setJavaScriptEnabled(true);
                popWebView.setWebViewClient(new WebViewClient());
                if (webViewCtrol == null) {
                    webViewCtrol = new WebViewCtrolImpl(PadMainActivity.this);
                }
                popWebView.setWebViewClient(new RjWebViewClient(webViewCtrol));
                popWebView.getSettings().setBuiltInZoomControls(false);// jeozey
                // 配合popwindow一定不能使用下面开关http://blog.csdn.net/changemyself/article/details/6969720
                popWebView.loadUrl(url);

                ImageView closeBtn = (ImageView) view
                        .findViewById(R.id.closeBtn);
                if (percent < 1) {
                    closeBtn.setVisibility(View.VISIBLE);
                    closeBtn.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            popupWindow.dismiss();
                        }
                    });
                } else {
                    closeBtn.setVisibility(View.GONE);
                }

                popupWindow.setAnimationStyle(R.style.AnimationPreview);
                popupWindow.showAtLocation(mainLinearLayout, Gravity.CENTER, 0,
                        0);

                if (url.indexOf("Meetting.htm") != -1) {
                    new Timer().schedule(new TimerTask() {

                        @Override
                        public void run() {
                            handler.sendEmptyMessage(12);
                        }
                    }, 5000);
                }

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("info", "landscape");
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("info", "portrait");
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 按下的如果是BACK，同时没有重复
            Log.e(TAG, "onKeyDown BACK");

            if (downloadDialogHandler != null) {
                dismissDownLoadDialog();
                cancleDownLoad = true;
            }

            if (isProgressDialogShowing()) {
                dismissProgressDialog();
            } else {
                exitApp();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        try {
            if (loadDialog != null) {
                loadDialog.dismiss();
            }
            if (downloadDialogHandler != null) {
                // 关闭下载进度条
                dismissDownLoadDialog();
                cancleDownLoad = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e(TAG, "DisplayMetrics:" + dm);
        // handler.sendEmptyMessage(18);// 取消转圈圈 (onActivityResult 发生在
        // onResume之前 所以pdf回调上传 不能取消掉转圈圈)
        super.onResume();
        Log.e(TAG, "onResume");
    }


    private static final int newVersionCode = 55;
    private static final String downUrl = "/wisp_aas/config/apk/WISP_PDF.apk";


    private HashMap<String, String> downLoadHwUrls = new HashMap<String, String>();
    private String lastDownLoadHwPath = "";


    /***************
     * E本pdf批注 （注意是否过期，过期的话是无法调用打开的）
     *********************/
    private static final String MIME_TYPE = "application/pdf";
    private static final String PACKAGE_NAME_ENTRY = "com.ebensz.pdf.nfschina.droid.sinohydro";
    private static final String ACTIVITY_NAME_ENTRY = "com.ebensz.pdf.nfschina.droid.sinohydro.pdfsign.droid.PdfViewerActivity";
    private static final int EPEN_REQUEST_CODE = 12;

    private void startPDFActivityForResult(String filePath) {
        Log.e(TAG, "E本pdf批注");
        Uri data = Uri.fromFile(new File(filePath));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(data, MIME_TYPE);
        intent.setComponent(new ComponentName(PACKAGE_NAME_ENTRY,
                ACTIVITY_NAME_ENTRY));
        startActivityForResult(intent, EPEN_REQUEST_CODE);
    }

    /***************
     * E本pdf批注
     *********************/

    // 批注的log/pdf/回调函数
    private String pdfDownLoadUrl = "";
    private String logDownLoadUrl = "";
    private String pdfAnnoCallBack = "";

    // 下载批注的lpdf

    // 下载批注的log

    // 显示confirm对话框
    private String callBack;


    public void clearCache(boolean disk) {
        if (leftFragment != null) {
            leftFragment.clearCache(disk);
        }
    }

    private TabMenu.MenuBodyAdapter bodyAdapter = new TabMenu.MenuBodyAdapter(
            this, new int[]{R.mipmap.menu_fresh, R.mipmap.menu_reset,
            R.mipmap.menu_setting, R.mipmap.menu_exit},
            new String[]{"刷新", "重置", "设置", "退出"});
    protected TabMenu tabMenu;

    class BodyClickEvent implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long arg3) {
            tabMenu.SetBodySelect(position, Color.GRAY);
            Log.i("Log", " BodyClickEvent implements OnItemClickListener "
                    + position);
            onTapMenuSelect(position);
            tabMenu.dismiss();
        }

    }

    private void reset() {
        clearCache(true);
//        SystemUtil.clearCookies(getBaseContext());
        WispCore.getWISPSO().CloseService();
        startActivity(new Intent(PadMainActivity.this, LoginActivity.class));
    }

    private void onTapMenuSelect(int position) {
        switch (position) {
            case 0:
                reload();
                break;
            case 1:
                reset();
                break;
            case 2:
                new AppSettingDialog(PadMainActivity.this).show();
                break;
            case 3:
                exitApp();
                break;
            default:
                break;
        }
    }

    @Override
    /**
     * 创建MENU
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");// 必须创建一项
        return super.onCreateOptionsMenu(menu);
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
                tabMenu.showAtLocation(mainLinearLayout, Gravity.BOTTOM, 0, 0);
            }
        }
        return false;// 返回为true 则显示系统menu
    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) { MenuItem
     * refreshItem = menu.add(0, 0, 0, "刷新");
     * refreshItem.setIcon(R.drawable.ic_menu_refresh);
     * 
     * MenuItem clearItem = menu.add(0, 1, 1, "重置");
     * clearItem.setIcon(R.drawable.menu_delete);
     * 
     * MenuItem settingItem = menu.add(0, 2, 2, "设置");
     * settingItem.setIcon(R.drawable.icon_setting_48);
     * 
     * MenuItem quitItem = menu.add(0, 3, 3, "退出");
     * quitItem.setIcon(R.drawable.ic_menu_quit);
     * 
     * return super.onCreateOptionsMenu(menu); }
     * 
     * public boolean onMenuItemSelected(int featureId, MenuItem item) { switch
     * (item.getItemId()) { case 0: // leftFragment.loadUrl(DB.PRE_URL +
     * DB.HOMEPAGE_URL); reload(); return false; case 1: clearCache(true);
     * SystemUtil.clearCookies(getBaseContext());
     * WISP.getWISPSO().CloseService(); startActivity(new
     * Intent(PadMainActivity.this, LoginActivity.class)); return false; case 2:
     * new AppSettingDialog(PadMainActivity.this).show(); break; case 3:
     * exitApp(); break; default: break; } return
     * super.onMenuItemSelected(featureId, item); }
     */
    public void exitApp() {
        AlertDialog.Builder alertbBuilder = new AlertDialog.Builder(this);

        alertbBuilder.setTitle("系统提示").setMessage("您确定退出程序吗?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 停止8011监听服务
                        WispCore.getWISPSO().CloseService();

                        // 删除临时文件
//                        FileUtil.deleteTempFile();

                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                android.os.Process
                                        .killProcess(android.os.Process.myPid());
                                // Intent data = new Intent();
                                // setResult(-1, data);
                                // finish();
                            }
                        }, 1000);

                    }

                }).create();

        alertbBuilder.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingBtn:
                if (settingPopMenu != null)
                    settingPopMenu.showAsDropDown(v);
                break;
            case R.id.userBtn:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        try {
//            if (screenOnReceiver != null) {
//                unregisterReceiver(screenOnReceiver);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}