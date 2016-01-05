package com.rj.wisp.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.rj.framework.AppSystemTool;
import com.rj.framework.DB;
import com.rj.framework.SharedPreferencesUtil;
import com.rj.util.AndroidTool;
import com.rj.view.ToastTool;
import com.rj.wisp.R;
import com.rj.wisp.activity.AppLoadActivity;
import com.rj.wisp.core.WispCore;
import com.rj.wisp.task.GetAppConfigTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class AppSettingDialog extends Dialog implements View.OnClickListener {
    private static final String TAG = AppSettingDialog.class.getName();
    private static final float BEISHU = 0.6f;

    private Context context;
    private LayoutInflater layoutInflater;
    private View contentView;
    private View settingLyt, aboutUsLyt, aboutUsUpLyt;
    private EditText ipEt, portEt, appNameEt;
    private EditText vpnChooeseEt, vpnHostEt, vpnPortEt, vpnUserEt, vpnPwdEt;
    private Button aboutUsBtn, logBtn, updateBtn, okBtn, cancelBtn, goBackBtn;
    private TextView aboutUsVersionTv;
    private AlertDialog.Builder appNameDialogBuilder;
    private int rowIndex;
    private ViewAnimator mSwitcher;

    enum SwitcherMode {
        SETTING, ABOUT
    }

    private SwitcherMode mSwitcherMode = SwitcherMode.SETTING;

    public AppSettingDialog(Context context) {
        super(context);
        this.context = context;
        constructor();
    }

    public AppSettingDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        constructor();
    }

    private void constructor() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        layoutInflater = LayoutInflater.from(context);
        contentView = layoutInflater.inflate(R.layout.app_setting_dialog_lyt, null);
        mSwitcher = (ViewAnimator) contentView.findViewById(R.id.switcher);
        mSwitcherMode = SwitcherMode.SETTING;
        mSwitcher.setDisplayedChild(mSwitcherMode.ordinal());
        try {
            Activity activity = (Activity) context;
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            LayoutParams lp = getWindow().getAttributes();
            lp.width = (int) (displayMetrics.widthPixels * BEISHU);
            lp.height = (int) (displayMetrics.heightPixels * BEISHU);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(contentView);
        // setCanceledOnTouchOutside(false);
        setCanceledOnTouchOutside(true);

        if (!TextUtils.isEmpty(DB.VPN_HOST)) {
            contentView.findViewById(R.id.vpnTableLayout).setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.lineTableRow).setVisibility(View.VISIBLE);
        }
        // 初始化工作
        ipEt = (EditText) contentView.findViewById(R.id.setting_ip_et);
        portEt = (EditText) contentView.findViewById(R.id.setting_port_et);
        vpnChooeseEt = (EditText) contentView.findViewById(R.id.setting_vpn_chooese_et);
        vpnHostEt = (EditText) contentView.findViewById(R.id.setting_vpn_host_name_et);
        vpnPortEt = (EditText) contentView.findViewById(R.id.setting_vpn_port_name_et);
        vpnUserEt = (EditText) contentView.findViewById(R.id.setting_vpn_user_name_et);
        vpnPwdEt = (EditText) contentView.findViewById(R.id.setting_vpn_pwd_name_et);
        appNameEt = (EditText) contentView.findViewById(R.id.dialogsetting_name_et);
        ipEt.setText("" + DB.SECURITY_HOST);
        portEt.setText("" + DB.SECURITY_PORT);
        vpnChooeseEt.setText(DB.USE_VPN ? "使用" : "不使用");
        vpnChooeseEt.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    showVPNSettings((EditText) v);
                }
                return false;
            }
        });
        vpnHostEt.setText("" + DB.VPN_HOST);
        vpnPortEt.setText("" + DB.VPN_PORT);
        vpnUserEt.setText("" + DB.VPN_USER);
        vpnPwdEt.setText("" + DB.VPN_PASS);
        portEt.setText("" + DB.SECURITY_PORT);
        appNameEt.setText("" + DB.APP_NAME);
        appNameEt.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i("GuLang", "onTouch-/*");
                    //重启新的HTTP Server监听
                    WispCore.getWISPSO().restartHttpServer();

                    final GetAppConfigTask getAppConfigTask = new GetAppConfigTask(

                            context, new GetAppConfigTask.GetAppConfigCallBack() {
                        @Override
                        public void callback(String jsonStr) {
                            // appSettingCallBack.callBack(editText,
                            // jsonStr);

                            ArrayList<HashMap<String, String>> result = handleData(jsonStr);
                            if (result.size() == 1) {
                                setConfig(appNameEt, result.get(0));
                            } else {
                                showAppSettings(appNameEt, result);
                            }

                            Log.i("GuLang", "" + jsonStr);
                        }
                    });

                    DB.SECURITY_HOST = ipEt.getText().toString();
                    try {
                        DB.SECURITY_PORT = Integer.parseInt(portEt.getText().toString() + "");
                    } catch (NumberFormatException e1) {
                        DB.SECURITY_PORT = 0;
                    }
                    DB.APP_NAME = appNameEt.getText().toString();

                    // 隐藏软键盘
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

//					final GetAppConfigDialog appLoadDialog = new GetAppConfigDialog(v.getContext());
//					appLoadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//					appLoadDialog.setTitle("");
//					appLoadDialog.setMessage("正在获取应用列表...");
//					appLoadDialog.setOnCancelListener(new OnCancelListener() {
//						@Override
//						public void onCancel(DialogInterface dialog) {
//							getAppConfigTask.cancel(true);
//						}
//					});
                    getAppConfigTask.execute();
                }
                return false;
            }
        });

        settingLyt = contentView.findViewById(R.id.setting_lyt);
        aboutUsLyt = contentView.findViewById(R.id.about_us_lyt);

        aboutUsBtn = (Button) contentView.findViewById(R.id.settingdialog_aboutus_btn);
        logBtn = (Button) contentView.findViewById(R.id.settingdialog_log_btn);
        updateBtn = (Button) contentView.findViewById(R.id.settingdialog_update_btn);
        okBtn = (Button) contentView.findViewById(R.id.settingdialog_ok_btn);
        cancelBtn = (Button) contentView.findViewById(R.id.settingdialog_cancel_btn);
        goBackBtn = (Button) contentView.findViewById(R.id.goback_btn);

        if (!DB.CHANGE_HOST) {
            ipEt.setEnabled(false);
            portEt.setEnabled(false);
            appNameEt.setEnabled(false);
        }

        aboutUsBtn.setOnClickListener(this);
        logBtn.setOnClickListener(this);
        updateBtn.setOnClickListener(this);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        goBackBtn.setOnClickListener(this);

        // 设置版本号
        aboutUsVersionTv = (TextView) contentView
                .findViewById(R.id.about_us_version_tv);
        aboutUsVersionTv.setText("版本号：" + AndroidTool.getVersionCode(context));

        aboutUsUpLyt = contentView.findViewById(R.id.about_us_up_lyt);

        appNameDialogBuilder = new AlertDialog.Builder(context).setTitle("应用列表").setNegativeButton(
                "取消", new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

    }

    private void showVPNSettings(final EditText editText) {
        try {
            appNameDialogBuilder = new AlertDialog.Builder(context).setTitle("是否使用VPN")
                    .setNegativeButton("取消", new OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

            final String[] data = new String[]{"使用", "不使用"};
            if (DB.USE_VPN) {
                rowIndex = 0;
            } else {
                rowIndex = 1;
            }
            appNameDialogBuilder.setIcon(R.drawable.ic_launcher)
                    .setSingleChoiceItems(data, rowIndex,
                            new OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    if (editText != null) {
                                        editText.setText(data[which]);
                                    }

                                    DB.USE_VPN = "使用".endsWith(data[which]);
                                    dialog.cancel();

                                }
                            });

            AlertDialog alertDialog = appNameDialogBuilder.create();
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setConfig(final EditText editText, Map<String, String> map) {
        DB.AAS_HOST = map.get("address");
        DB.APP_NAME = map.get("name");
        DB.AAS_PORT = Integer.parseInt(map.get("port"));
        DB.APP_CODE = map.get("appcode");
        DB.LOGINPAGE_URL = map.get("loginpage");
        DB.HOMEPAGE_URL = map.get("homepage");
        DB.APP_CHARSET = map.get("charset");
        if (editText != null) {
            editText.setText(DB.APP_NAME);
        }
        ToastTool.show(context, "设置成功", Toast.LENGTH_SHORT);

    }
    private void showAppSettings(final EditText editText,
                                 final ArrayList<HashMap<String, String>> dataMapList) {
        if (dataMapList == null || dataMapList.size() == 0) {
            return;
        }
        final String[] data = new String[dataMapList.size()];
        int i = 0;
        for (HashMap<String, String> hashMap : dataMapList) {
            data[i++] = hashMap.get("name");
        }
        appNameDialogBuilder.setIcon(R.drawable.ic_launcher).setSingleChoiceItems(data, rowIndex,
                new OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> map = dataMapList.get(which);
                        setConfig(editText, map);
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = appNameDialogBuilder.create();
        alertDialog.show();
    }

    private ArrayList<HashMap<String, String>> handleData(String jsonData) {
        ArrayList<HashMap<String, String>> dataMapList = new ArrayList<HashMap<String, String>>();
        try {
            if (!TextUtils.isEmpty(jsonData)) {
                JSONArray jsonArray = new JSONArray(jsonData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", jsonObject.optString("name"));
                    map.put("appcode", jsonObject.optString("AppCode"));
                    map.put("address", jsonObject.optString("address"));
                    map.put("port", jsonObject.optString("port"));
                    map.put("loginpage", jsonObject.optString("loginpage"));
                    map.put("homepage", jsonObject.optString("homepage"));
                    map.put("charset", jsonObject.optString("Charset"));
                    dataMapList.add(map);
                    String address = jsonObject.optString("address", "");
                    String port = jsonObject.optString("port", "");
                    if (address.equals(DB.AAS_HOST) && port.equals(String.valueOf(DB.AAS_PORT))) {
                        rowIndex = i;
                    }
                }
            } else {
                ToastTool.show(context, "获取应用列表为空", Toast.LENGTH_SHORT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastTool.show(context, "应用列表解析出错", Toast.LENGTH_SHORT);
        }

        return dataMapList;
    }

    @Override
    public void onClick(View v) {
        Log.i("GuLang", "onClickBtn");
        switch (v.getId()) {
            //关于我们
            case R.id.settingdialog_aboutus_btn:
//			settingLyt.setVisibility(View.GONE);
//			aboutUsLyt.setVisibility(View.VISIBLE);
                mSwitcherMode = SwitcherMode.ABOUT;
                mSwitcher.setDisplayedChild(mSwitcherMode.ordinal());
                Animation animation = AnimationUtils.loadAnimation(context,
                        R.anim.about_us_animation);
                aboutUsUpLyt.startAnimation(animation);

                break;
            //获取日志
            case R.id.settingdialog_log_btn:
//			new LogUtils().generateLog(context,new LogCollectorFile(context) {
//			}, new String[]{""});
                break;
            case R.id.settingdialog_update_btn:
                Toast.makeText(context, "更新按钮", Toast.LENGTH_SHORT).show();
                // 其它操作...
                break;
            case R.id.settingdialog_cancel_btn:
                this.dismiss();
                break;
            case R.id.settingdialog_ok_btn:
                saveConfig();
                this.dismiss();
                break;
            case R.id.goback_btn:
//			settingLyt.setVisibility(View.VISIBLE);
//			aboutUsLyt.setVisibility(View.GONE);
                mSwitcherMode = SwitcherMode.SETTING;
                mSwitcher.setDisplayedChild(mSwitcherMode.ordinal());
                break;

            default:
                break;
        }
    }

    private void saveConfig() {
        DB.SECURITY_HOST = ipEt.getText().toString();
        try {
            DB.SECURITY_PORT = Integer.parseInt(portEt.getText().toString() + "");
        } catch (NumberFormatException e1) {
            DB.SECURITY_PORT = 0;
        }
        try {
            DB.VPN_HOST = vpnHostEt.getText().toString();
            DB.VPN_USER = vpnUserEt.getText().toString();
            DB.VPN_PASS = vpnPwdEt.getText().toString();
            DB.VPN_PORT = Integer.parseInt(vpnPortEt.getText().toString() + "");
        } catch (NumberFormatException e1) {
            DB.VPN_PORT = 0;
        }


        DB.APP_NAME = appNameEt.getText().toString();

        try {
            /** Sharedpreferences方式读存储配置： */
            Editor editor = SharedPreferencesUtil.getSharedPreferences(context).edit();

            editor.putString("APP_CODE", DB.APP_CODE);
            Log.e("MMM", "appCode:" + DB.APP_CODE);
            editor.putString("APP_CHARSET", DB.APP_CHARSET);

            editor.putString("APP_NAME", DB.APP_NAME);
            editor.putString("SECURITY_HOST", DB.SECURITY_HOST);
            editor.putInt("SECURITY_PORT", DB.SECURITY_PORT);
            editor.putString("AAS_HOST", DB.AAS_HOST);
            editor.putInt("AAS_PORT", DB.AAS_PORT);
            editor.putString("LOGINPAGE_URL", DB.LOGINPAGE_URL);
            editor.putString("HOMEPAGE_URL", DB.HOMEPAGE_URL);

            editor.putBoolean("useVpn", DB.USE_VPN);
            editor.putString("vpnHost", DB.VPN_HOST);
            editor.putInt("vpnPort", DB.VPN_PORT);
            editor.putString("vpnUser", DB.VPN_USER);
            editor.putString("vpnPass", DB.VPN_PASS);


            editor.putBoolean("hasStoreFlag", true);// 存储过这些变量，则从SharedPreferences取
            // 不从xml取
            editor.commit();

            AppSystemTool.clearWebViewCookie(context);
            AppSystemTool.restartApp((Activity) context, AppLoadActivity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Log.i(TAG,
                    "(" + event.getX() + "," + event.getY() + "),contentView.getWidth()="
                            + contentView.getWidth() + ",contentView.getHeight()="
                            + contentView.getHeight());
            if (!(event.getX() >= -10 && event.getY() >= -10)
                    || event.getX() >= contentView.getWidth() + 10
                    || event.getY() >= contentView.getHeight() + 20) {// 如果点击位置在当前View外部则销毁当前视图,其中10与20为微调距离
                InputMethodManager imm = (InputMethodManager) context
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
        return true;
    }
}
