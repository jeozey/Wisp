package com.rj.wisp.ui.phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.rj.framework.AppSystemTool;
import com.rj.framework.DB;
import com.rj.util.PixelTool;
import com.rj.view.BackTitleBar;
import com.rj.view.ToastTool;
import com.rj.view.listview.CornerListView;
import com.rj.wisp.R;
import com.rj.wisp.activity.AppLoadActivity;
import com.rj.wisp.bean.AppConfig;
import com.rj.wisp.core.InitUtil;
import com.rj.wisp.task.GetAppConfigTask;
import com.rj.wisp.ui.adapter.PhoneSettingAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：志文 on 2015/12/15 0015 09:22
 * 邮箱：594485991@qq.com
 */
public class PhoneSettingActivity extends Activity implements View.OnClickListener, PhoneSettingAdapter.AppSettingCallBack {
    private static final String TAG = PhoneSettingActivity.class.getName();
    private BackTitleBar titleBar;
    private LinearLayout scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_phone_setting);
        titleBar = (BackTitleBar) findViewById(R.id.titleBar);
        titleBar.setLeftText("返回");
        titleBar.setRightText("保存");
        titleBar.setTitle("设置");
        titleBar.setBtnOnclickListener(this);

        scrollView = (LinearLayout) findViewById(R.id.setting_scrollView);

        initAppSetting();
        if (DB.USE_VPN) {
            initVpnSetting();
        }
        initAboutSetting();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.leftBtn:
                PhoneSettingActivity.this.finish();
                break;
            case R.id.rightBtn:
                InitUtil.saveConfig(getBaseContext());
                AppSystemTool.clearWebViewCookie(getBaseContext());
                AppSystemTool.restartApp(PhoneSettingActivity.this, AppLoadActivity.class);
                break;
            default:
                break;
        }
    }

    //解决 scrollview 与listview冲突问题 http://blog.csdn.net/hitlion2008/article/details/6737459
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        int margin = PixelTool.dip2px(getBaseContext(), 15);
        params.setMargins(margin, margin, margin, 0);
        listView.setLayoutParams(params);
    }

    private ArrayList<HashMap<String, String>> appConfigs;
    PhoneSettingAdapter appConfigAdapter;

    private void initAppSetting() {
        appConfigs = getAppSettingList();
        CornerListView listView = new CornerListView(getBaseContext());
        appConfigAdapter = new PhoneSettingAdapter(this, appConfigs
                , R.layout.simple_list_item_edit, new String[]{
                "item", "text"}, new int[]{R.id.item_title,
                R.id.item_edit}, this);
        listView.setAdapter(appConfigAdapter);

        initListView(listView);
        scrollView.addView(listView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setListViewHeightBasedOnChildren(listView);
//        scrollView.invalidate();
    }

    private void initVpnSetting() {
        CornerListView listView = new CornerListView(getBaseContext());
        PhoneSettingAdapter adapter = new PhoneSettingAdapter(this,
                getVpnSettingList(), R.layout.simple_list_item_edit, new String[]{
                "item", "text"}, new int[]{R.id.item_title,
                R.id.item_edit}, this);
        listView.setAdapter(adapter);

        initListView(listView);
        scrollView.addView(listView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setListViewHeightBasedOnChildren(listView);
//        scrollView.invalidate();
    }

    private void initAboutSetting() {
        CornerListView listView = new CornerListView(getBaseContext());
        PhoneSettingAdapter adapter = new PhoneSettingAdapter(this,
                getAboutList(), R.layout.simple_list_item_img_right, new String[]{
                "item"}, new int[]{R.id.item_title}, this);
        listView.setAdapter(adapter);

        initListView(listView);
        scrollView.addView(listView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setListViewHeightBasedOnChildren(listView);
//        scrollView.invalidate();
    }

    private void initListView(CornerListView listView) {
        if (listView != null) {

            listView.setDivider(new ColorDrawable(getResources().getColor(R.color.themeGray)));
            listView.setDividerHeight(PixelTool.dip2px(getBaseContext(), 1));
            listView.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_bg_corner_listview));
        }
    }

    public ArrayList<HashMap<String, String>> getAppSettingList() {
        ArrayList appSettingList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        HashMap<String, String> map2 = new HashMap<String, String>();
        HashMap<String, String> map3 = new HashMap<String, String>();
        map1.put("item", "应用地址:");
        map1.put("text", DB.SECURITY_HOST);

        map2.put("item", "平台端口:");
        map2.put("text", DB.SECURITY_PORT + "");

        map3.put("item", "应用选择:");
        map3.put("text", DB.APP_NAME);

        appSettingList.add(map1);
        appSettingList.add(map2);
        appSettingList.add(map3);

        return appSettingList;

    }

    public ArrayList<HashMap<String, String>> getVpnSettingList() {
        ArrayList vpnSettingList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        HashMap<String, String> map2 = new HashMap<String, String>();
        HashMap<String, String> map3 = new HashMap<String, String>();
        HashMap<String, String> map4 = new HashMap<String, String>();
        HashMap<String, String> map5 = new HashMap<String, String>();


        map1.put("item", "是否使用VPN拨号:");
        map1.put("text", DB.USE_VPN ? "使用" : "不使用");

        map2.put("item", "VPN地址:");
        map2.put("text", DB.VPN_HOST);

        map3.put("item", "VPN端口:");
        map3.put("text", "" + DB.VPN_PORT);

        map4.put("item", "VPN帐号:");
        map4.put("text", DB.VPN_USER);

        map5.put("item", "VPN密码:");
        map5.put("text", DB.VPN_PASS);

        vpnSettingList.add(map1);
        vpnSettingList.add(map2);
        vpnSettingList.add(map3);
        vpnSettingList.add(map4);
        vpnSettingList.add(map5);
        return vpnSettingList;
    }

    public ArrayList<HashMap<String, String>> getAboutList() {
        ArrayList appSettingList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("item", "关于我们:");
        appSettingList.add(map1);

        return appSettingList;

    }


    @Override
    public void callBack(EditText editText, String jsonStr) {

    }

    private ArrayList<HashMap<String, String>> handleData(String jsonData) {
        ArrayList<HashMap<String, String>> dataMapList = new ArrayList<HashMap<String, String>>();
        try {
            if (!TextUtils.isEmpty(jsonData)) {
                jsonData = jsonData.replace("\r", "").replace("\n", "");
                List<AppConfig> list = JSON.parseArray(jsonData, AppConfig.class);
                int i = 0;
                for (AppConfig config : list) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", config.getName());
                    map.put("appcode", config.getAppcode());
                    map.put("address", config.getAddress());
                    map.put("port", "" + config.getPort());
                    map.put("loginpage", config.getLoginpage());
                    map.put("homepage", config.getHomepage());
                    map.put("charset", config.getCharset());
                    dataMapList.add(map);
                    String address = config.getAddress();
                    String port = "" + config.getPort();
                    if (address.equals(DB.AAS_HOST)
                            && port.equals(String.valueOf(DB.AAS_PORT))) {
                        rowIndex = i;
                    }
                }
            } else {
                ToastTool
                        .show(getBaseContext(), "获取应用列表为空", Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastTool.show(getBaseContext(), "应用列表解析出错", Toast.LENGTH_SHORT);
        }

        return dataMapList;
    }

    private int rowIndex = -1;

    AlertDialog alertDialog;

    private void showAppSettings(
            final ArrayList<HashMap<String, String>> dataMapList) {
        try {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            if (dataMapList == null || dataMapList.size() == 0) {
                return;
            }
            final String[] data = new String[dataMapList.size()];
            int i = 0;
            for (HashMap<String, String> hashMap : dataMapList) {
                data[i++] = hashMap.get("name");
            }
            AlertDialog.Builder appNameDialogBuilder = new AlertDialog.Builder(this).setTitle("应用列表")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            appNameDialogBuilder
                    .setSingleChoiceItems(data, rowIndex,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    Map<String, String> map = dataMapList
                                            .get(which);
                                    setConfig(map);
                                    dialog.cancel();

                                }
                            });
            alertDialog = appNameDialogBuilder.create();
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setConfig(Map<String, String> map) {
        DB.AAS_HOST = map.get("address");
        DB.APP_NAME = map.get("name");
        DB.AAS_PORT = Integer.parseInt(map.get("port"));
        DB.APP_CODE = map.get("appcode");
        DB.LOGINPAGE_URL = map.get("loginpage");
        DB.HOMEPAGE_URL = map.get("homepage");
        DB.APP_CHARSET = map.get("charset");
        Log.e(TAG, "DB.AAS_HOST:" + DB.AAS_HOST);
        Log.e(TAG, "DB.APP_CODE:" + DB.APP_CODE);
        Log.e(TAG, "DB.APP_CHARSET:" + DB.APP_CHARSET);


        if (appConfigs.size() > 2) {
            appConfigs.get(2).put("text", DB.APP_NAME);
        }
        appConfigAdapter.notifyDataSetChanged();

        ToastTool.show(this, "设置成功", Toast.LENGTH_SHORT);
    }
    @Override
    public void getAppInfo(View v, String securityHost, int securityPort) {
        //重启新的HTTP Server监听
//                WispCore.getWISPSO().CloseService();
//                WispCore.getWISPSO().StartService(new Handler(), getBaseContext());
//        WispCore.getWISPSO().restartHttpServer();

        // 回调到activity处理
        final GetAppConfigTask getAppConfigTask = new GetAppConfigTask(
                PhoneSettingActivity.this, new GetAppConfigTask.GetAppConfigCallBack() {
            @Override
            public void callback(String jsonStr) {
//                showAppSettings(handleData(jsonStr));

                ArrayList<HashMap<String, String>> result = handleData(jsonStr);
                if (result.size() == 1) {
                    setConfig(result.get(0));
                } else {
                    showAppSettings(result);
                }
            }
        });

        DB.SECURITY_HOST = securityHost;
        DB.SECURITY_PORT = securityPort;
        Log.e(TAG, "SECURITY_HOST:" + DB.SECURITY_HOST + " SECURITY_PORT:" + DB.SECURITY_PORT);
//
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        final ProgressDialog appLoadDialog = new ProgressDialog(
                PhoneSettingActivity.this);
        appLoadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        appLoadDialog.setTitle("");
        appLoadDialog.setMessage("正在获取应用列表...");
        appLoadDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                getAppConfigTask.cancel(true);

            }
        });
        getAppConfigTask.execute();
    }
}
