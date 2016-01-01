package com.rj.wisp.ui.phone;

/**
 * 作者：志文 on 2015/11/20 0020 09:53
 * 邮箱：594485991@qq.com
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rj.framework.ButtonFactory;
import com.rj.framework.DB;
import com.rj.framework.ErrorPageUtil;
import com.rj.framework.webview.UrlHandler;
import com.rj.framework.webview.WebViewCtrol;
import com.rj.view.PopMenu;
import com.rj.view.TabMenu;
import com.rj.view.ToastTool;
import com.rj.view.button.ButtonNum;
import com.rj.view.button.CustomButton;
import com.rj.view.button.CustomWidgetButton;
import com.rj.view.button.NoticeBean;
import com.rj.view.button.PhoneHorizontalBtns;
import com.rj.view.loading.CustomProgressDialog;
import com.rj.view.loading.ProgressDialogTool;
import com.rj.wisp.R;
import com.rj.wisp.activity.LoginActivity;
import com.rj.wisp.base.WispApplication;
import com.rj.wisp.bean.Attachment;
import com.rj.wisp.bean.AttachmentDownEvent;
import com.rj.wisp.bean.HandlerWhat;
import com.rj.wisp.core.AttachmentCacheUtil;
import com.rj.wisp.core.Commons;
import com.rj.wisp.core.FileOpenUtil;
import com.rj.wisp.core.WISPComponentsParser;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.service.NetConnectService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;


public class PhoneMainActivity extends FragmentActivity implements WebViewCtrol {
    private TextView titleTextView, userNameTextView;
    private ImageView settingMenuBtn;
    private LinearLayout topTitleBar;
    private PhoneHorizontalBtns bottomMenus;
    private ProgressDialog downAttachmentProgress;

    // 更多按钮视图
    private PopMenu settingPopMenu;
    private static final String TAG = "PhoneMainActivity";
    private PhoneLeftFragment leftFragment;
    private CustomProgressDialog loadDialog = null;
    private RelativeLayout noticeTitle;

    private TabMenu.MenuBodyAdapter bodyAdapter = new TabMenu.MenuBodyAdapter(
            this, new int[]{R.mipmap.menu_fresh, R.mipmap.menu_reset,
            R.mipmap.menu_setting, R.mipmap.menu_exit}, new String[]{"刷新", "重置", "设置", "退出"});
    private TabMenu tabMenu;

    @Override
    protected void onDestroy() {
        try {
            if (leftFragment != null) {
                leftFragment.destroyWebView();
            }
//            stopNoticeScorll();
//            if (screenOnReceiver != null) {
//                unregisterReceiver(screenOnReceiver);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
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
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.phone_main);

        initView();

        if (TextUtils.isEmpty(DB.LOGINPAGE_URL)) {
            ToastTool.show(PhoneMainActivity.this, "登陆页不存在,请联系管理员解决 ", 1);
        }

        // AppSystemTool.clearWebViewCookie(getBaseContext());
        try {
            WispCore.getWISPSO().StartService(handler, getBaseContext());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        leftFragment = new PhoneLeftFragment();
        transaction
                .add(R.id.left_fragment_layout, leftFragment, "leftfragment");

        transaction.commit();

    }

    public interface ActivityHandlerListener {
        void handleWebView(String url);
    }

    public void closeFormView() {
        if (leftFragment != null) {
            leftFragment.closeFormView();
        }
    }

    public void clearCache(boolean disk) {
        if (leftFragment != null) {
            leftFragment.clearCache(disk);
        }
    }

    private void goBack() {
        leftFragment.goBack();
    }

    private void reload() {
        leftFragment.reload();
    }

    private Map<String, AttachmentDownEvent> attachmentDownEvents;

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

    private void handleAttachmentDownOver(AttachmentDownEvent event) {
        String downUrl = event.getDownUrl();
        if (Commons.ATTACHMENT_DOWN_CACHE == event.getDownResult()) {
            Log.e(TAG, "ATTACHMENT_DOWN_CACHE:" + event);
            ToastTool.show(PhoneMainActivity.this, "已经下载过了", Toast.LENGTH_LONG);

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
                ToastTool.show(PhoneMainActivity.this, "下载完毕", Toast.LENGTH_LONG);
            } else {
                ToastTool.show(PhoneMainActivity.this, "" + event.getDownFailMsg(), Toast.LENGTH_LONG);
            }
            try {
                Intent intent = FileOpenUtil.openFile(attachment, getBaseContext());
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                ex.printStackTrace();
                String contentType = e.getContentType();
                ToastTool.show(PhoneMainActivity.this, "没有合适的软件打开该附件 " + (contentType != null ? contentType : ""), Toast.LENGTH_SHORT);
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
                        topTitleBar.setVisibility(View.GONE);
                        bottomMenus.setVisibility(View.GONE);
                        break;
                    case HandlerWhat.LOG_OUT:
                        logOut();
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

    private void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            ToastTool.show(PhoneMainActivity.this, msg, Toast.LENGTH_SHORT);
        }
    }

    private void handWriting(Message msg) {
        if (msg.obj == null) {
            ToastTool.show(PhoneMainActivity.this, "获取手写请求出错", Toast.LENGTH_SHORT);
        } else {
            try {
//               HandWriting handWriting = WISPComponentsParser.getHandWritingProperty(msg.obj.toString());
//                if (handWriting != null) {
//                    String downLoadUrl = handWriting.getDownLoadUrl();
//                    //说明已经本地写入webview
//                    if(downLoadUrl.indexOf("@@")!=-1){
//                        openHandWritingWindow("@@", null);
//                    }else if (!TextUtils.isEmpty(downLoadUrl)) {
//                        String url = downLoadUrl;
//                        if (downLoadUrl.indexOf(DB.HTTPSERVER_HOST) != -1) {
//                            url = downLoadUrl.substring(
//                                    downLoadUrl.indexOf("/wisp_aas"),
//                                    downLoadUrl.length());
//                        }
//                        url = url.replace("\\", "/").replace("attachment",
//                                "newAttachment");// 截取url
//                        new HttpSocketTask(url.replace(".png", ".hw"),
//                                WispApplication.cookies, getBaseContext(), null)
//                                .execute(0);
//                    } else {
//                        openHandWritingWindow(null, null);
//                    }
//
//                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastTool.show(PhoneMainActivity.this, "手写出错", Toast.LENGTH_SHORT);
            }
        }
    }
    private void showDialog(Message msg) {
        if (msg.obj != null) {
            try {
                String type = msg.getData().getString("type");
                String content = msg.getData().getString("text");
                String title = msg.getData().getString("title");
                final String callBack = msg.getData().getString("callBack");

                AlertDialog.Builder customBuilder = new AlertDialog.Builder(PhoneMainActivity.this).setTitle(title).setMessage(content);
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

        leftFragment.loadUrl("javascript:try{ "
                + callBack
                + "; }catch(e){alert(e);}");
    }

    private void logOut() {
        Log.e(TAG, "logOut WispApplication.isLogin:" + WispApplication.isLogin);
        Log.e(TAG, "logOut logoutEvent:" + logoutEvent);
        if (!TextUtils.isEmpty(logoutEvent)) {
            leftFragment.loadUrl(logoutEvent);
        } else {
            ToastTool.show(PhoneMainActivity.this, "未获取到注销事件!", Toast.LENGTH_SHORT);
            Log.e(TAG, "注销事件为空");
        }
    }


    private boolean isProgressDialogShowing() {
        return ((loadDialog != null && loadDialog.isShowing()));
    }

    private void showProgressDialog() {
        try {
            if (loadDialog != null && !loadDialog.isShowing()) {
                loadDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        try {
            if (loadDialog != null && loadDialog.isShowing()) {
                loadDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

    private void showSetting() {
        startActivity(new Intent(PhoneMainActivity.this,
                SettingActivity.class));
    }

    protected void onTapMenuSelect(int position) {
        switch (position) {
            case 0:
                reload();
                break;
            case 1:
                clearCache(true);
//                SystemUtil.clearCookies(getBaseContext());
                WispCore.getWISPSO().CloseService();
                startActivity(new Intent(PhoneMainActivity.this,
                        LoginActivity.class));
                break;
            case 2:
                showSetting();
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
     * 拦截MENU
     */
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (tabMenu != null) {
            if (tabMenu.isShowing())
                tabMenu.dismiss();
            else {
                tabMenu.showAtLocation(findViewById(R.id.mainLinearLayout),
                        Gravity.BOTTOM, 0, 0);
            }
        }
        return false;// 返回为true 则显示系统menu
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
    protected void onPause() {
        Log.e(TAG, "onPause");
        if (settingPopMenu != null) {
            settingPopMenu.dismiss();
        }
        if (loadDialog != null && !loadDialog.isShowing()) {
            loadDialog.dismiss();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume");
        handler.sendEmptyMessage(18);// 取消转圈圈
        WispCore.getWISPSO().StartService(handler, PhoneMainActivity.this);
        super.onResume();
    }


    private DisplayMetrics dm;

    private void initView() {
        try {

            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            topTitleBar = (LinearLayout) findViewById(R.id.top_navigate_bar);
            bottomMenus = (PhoneHorizontalBtns) findViewById(R.id.bottom_navigate_bar);

            noticeTitle = (RelativeLayout) findViewById(R.id.noticeTitle);

            tabMenu = new TabMenu(this, new BodyClickEvent(), 0);// 出现与消失的动画
            tabMenu.update();
            tabMenu.SetBodyAdapter(bodyAdapter);


            loadDialog = new ProgressDialogTool()
                    .getProgressDialog(PhoneMainActivity.this);

            attachmentDownEvents = new HashMap<>();

            downAttachmentProgress = new ProgressDialog(PhoneMainActivity.this);
            downAttachmentProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            downAttachmentProgress.setMessage(getResources().getString(R.string.downloading));
            downAttachmentProgress.setCancelable(false);
//        downAttachmentProgress.setOnKeyListener(onKeyListener);


            titleTextView = (TextView) findViewById(R.id.top_title);
            titleTextView.setText("首页");

            userNameTextView = (TextView) findViewById(R.id.top_user_name);
            userNameTextView
                    .setWidth(this.getResources().getDisplayMetrics().widthPixels / 4);
            userNameTextView.setText("用户");
            userNameTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(PhoneMainActivity.this,
                            "" + userNameTextView.getText().toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            settingMenuBtn = (ImageView) findViewById(R.id.setting_menu);

            settingPopMenu = new PopMenu(PhoneMainActivity.this, handler,
                    new PopMenu.PopMenuOnItemClickListener() {

                        @Override
                        public void onItemClick(int index) {
                            switch (index) {
                                case 0:
                                    handler.sendEmptyMessage(HandlerWhat.LOG_OUT);
                                    break;
                                case 1:
                                    showSetting();
                                    break;

                                default:
                                    break;
                            }
                        }
                    });
            // 初始化弹出菜单
            settingMenuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    settingPopMenu.showAsDropDown(v);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            ToastTool.show(PhoneMainActivity.this, "初始化布局出错", Toast.LENGTH_LONG);
        }


    }

    private String logoutEvent = "";
    private List<CustomButton> collectionlist;// 更多按钮
    private List<NoticeBean> noticeList;

    // 初始化动态按钮
    public void updateBottomButtonBar(String data) {
        try {

            Log.e(TAG, "buttonsJson:" + data);

            Map<String, List> map = WISPComponentsParser
                    .getCustomButtonList4Json(data);
            collectionlist = map.get(WISPComponentsParser.MORE_MENUS_LIST);
            List<CustomButton> buttonlist = map.get(WISPComponentsParser.BOTTOM_MENUS_LIST);

            /***
             * 添加顶部公告栏
             */
            noticeList = map.get(WISPComponentsParser.NOTICE_MENUS_LIST);
            if (noticeList != null && noticeList.size() > 0) {
                noticeTitle.setVisibility(View.VISIBLE);
            }


            ArrayList<CustomWidgetButton> list = new ArrayList<CustomWidgetButton>();


            CustomWidgetButton info;
            for (CustomButton customButton : buttonlist) {
                if ("menubtn".equalsIgnoreCase(customButton.getType())) {
                    info = new CustomWidgetButton();
                    info.setBeforeImg(ButtonFactory.getDrawable(getBaseContext(),
                            customButton.getBeforeImg()));
                    info.setAfterImg(ButtonFactory.getDrawable(getBaseContext(),
                            customButton.getAfterImg()));
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
                } else if ("title".equalsIgnoreCase(customButton.getType())) {
                    String user = customButton.getButtonText();
                    userNameTextView.setText(user);
                } else if ("logout".equalsIgnoreCase(customButton.getType())) {
                    logoutEvent = customButton.getClickEvent();
                    settingMenuBtn.setVisibility(View.VISIBLE);
                }
            }
            boolean flg = false;
            for (CustomWidgetButton customWidgetButton : list) {
                if (customWidgetButton != null && "更多".equals(customWidgetButton.getTitle())) {
                    flg = true;
                    break;
                }
            }
            if (!flg && collectionlist != null && collectionlist.size() > 0) {
                CustomWidgetButton leftBtn;
                leftBtn = new CustomWidgetButton();
                leftBtn.setTitle("更多");
                leftBtn.setType(CustomWidgetButton.ButtonType.LeftBtn);

                leftBtn.setBeforeImg(getResources().getDrawable(R.mipmap.more_btn));
                leftBtn.setAfterImg(getResources().getDrawable(R.mipmap.more_btn));

                CustomButton more = collectionlist.get(0);
                if (more != null) {
                    leftBtn.setBeforeImg(ButtonFactory.getDrawable(getBaseContext(),
                            more.getBeforeImg()));
                    leftBtn.setAfterImg(ButtonFactory.getDrawable(getBaseContext(),
                            more.getAfterImg()));
                }

                list.add(0, leftBtn);
            }


            if (list == null || list.size() == 0) {
                bottomMenus.setVisibility(View.GONE);
            } else {
                bottomMenus.setVisibility(View.VISIBLE);
                bottomMenus.init(list, new PhoneHorizontalBtns.HorizontalBtnsCallBack() {
                    @Override
                    public void callBack(CustomWidgetButton info) {
                        if (CustomWidgetButton.ButtonType.LeftBtn == info.getType() || "更多".equals(info.getTitle())) {
                            Intent intent = new Intent(PhoneMainActivity.this,
                                    MoreActivity.class);
                            intent.putExtra("buttons",
                                    (Serializable) collectionlist);
                            startActivityForResult(intent, MORE_ACTIVITY_RESULT_CODE);
                        } else {
                            if (!TextUtils.isEmpty(info.getCallBack())) {
                                String title = info.getTitle();
                                if (!TextUtils.isEmpty(title)) {
                                    titleTextView.setText(title);
                                }
                                leftFragment.loadUrl(info.getCallBack());
                            }
                        }

                    }
                });
                if (!"更多".equals(list.get(0).getTitle())) {
                    titleTextView.setText(list.get(0).getTitle());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int MORE_ACTIVITY_RESULT_CODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (resultCode) {
                case MORE_ACTIVITY_RESULT_CODE:
                    onMoreActivityBack(data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMoreActivityBack(Intent data) {
        if (data != null) {
            String url = data.getStringExtra("url");
            String title = data.getStringExtra("title");
            if (!TextUtils.isEmpty(url)) {
                leftFragment.loadUrl(url);
            }
            if (!TextUtils.isEmpty(title)) {
                titleTextView.setText(title);
            }
        }
    }

    //动态添加界面按钮
    private void addWebUI(Message msg) {
        topTitleBar.setVisibility(View.VISIBLE);

        if (leftFragment != null && leftFragment.isFormViewOpen()) {
            Log.e(TAG, "msgD" + msg.obj);
            leftFragment.updateBottomTabBar(msg.obj.toString());
        } else {
            Log.e(TAG, "msgD1" + msg.obj.toString());
            String data = msg.obj.toString();
            // 只有注销按钮的情况也要获取注销事件
            if (data != null && (data.indexOf("menubtn") != -1)
                    || (data.indexOf("logout") != -1)) {
                updateBottomButtonBar(data);
            }
        }
    }


    // 更新条数
    public void addWebBtnNum(Message msg) {
        try {
            ButtonNum btnNum = (ButtonNum) msg.obj;
            String type = btnNum.getType();
            String buttonText = btnNum.getButtonText();
            String number = btnNum.getNumber();
            Log.e(TAG, "type:" + type);
            //更多里面的条数
            if ("appbtn".equals(type)) {
                CustomButton b = new CustomButton();
                b.setButtonText(buttonText);
                b.setNumber(number);

                updateMoreBtns(b);
            } else {
                CustomWidgetButton button;
                button = new CustomWidgetButton();
                button.setTitle(buttonText);
                button.setNum(Integer.valueOf(number));

                bottomMenus.updateBtn(button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMoreBtns(CustomButton b) {
        try {
            //更多
            for (CustomButton customButton : collectionlist) {
                //父级
                for (CustomButton pBtn : customButton.getCollction()) {
                    //子级
                    for (CustomButton btn : pBtn.getCollction()) {
                        if (b.getButtonText().equals(btn.getButtonText())) {
                            Log.e(TAG, "updateMoreBtns:");
                            btn.setNumber(b.getNumber());
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /******************************
     * webview 接口回调
     ***************************************/

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // 拦截tel sms mailto
        Log.i(TAG, "shouldOverrideUrlLoading url=" + url);
        if (UrlHandler.startUrl(PhoneMainActivity.this, view, url)) {
            view.stopLoading();
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        try {
            Log.v(TAG, "onPageStarted:" + url);
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

    }

    @Override
    public void onCloseWindow(WebView webView) {

    }

    @Override
    public void onJsAlert(WebView view, String url, String message,
                          JsResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                PhoneMainActivity.this);

        builder.setTitle(DB.APP_NAME).setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                    }
                });
        builder.setOnKeyListener(new OnKeyListener() {

            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                return true;
            }
        });
        // 禁止响应按back键的事件
        builder.setCancelable(false);

        try {
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
    }

    @Override
    public void onJsConfirm(WebView view, String url, String message,
                            final JsResult result) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                PhoneMainActivity.this);
        builder.setTitle(DB.APP_NAME).setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Log.e(TAG, "onReceivedError:");
        view.stopLoading();
        view.loadData(ErrorPageUtil.getErrorPage(getBaseContext(), failingUrl), "text/html", "utf-8");
    }

    /******************************
     * webview 接口回调 end
     ***************************************/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 按下的如果是BACK，同时没有重复
            Log.e(TAG, "KEYCODE_BACK");
            if (isProgressDialogShowing()) {
                dismissProgressDialog();
            }
//            if (downloadDialogHandler != null) {
//                dismissDownLoadDialog();
//                cancleDownLoad = true;
//            }
            if (leftFragment.isFormViewOpen()) {
                leftFragment.closeFormView();
            } else {
                exitApp();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

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
                        // 取消网络监听悬浮窗
                        Intent exipAppIntent = new Intent(
                                PhoneMainActivity.this, NetConnectService.class);
                        exipAppIntent.putExtra("FLAG_IS_EXIT", true);
                        startService(exipAppIntent);

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


    private PopupWindow popupWindow;
    private WebView popWebView;

    @Override
    public void destoryWebViewCtrol() {

    }
}