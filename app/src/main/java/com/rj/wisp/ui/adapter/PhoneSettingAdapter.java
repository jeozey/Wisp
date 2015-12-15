package com.rj.wisp.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rj.framework.DB;
import com.rj.view.ToastTool;
import com.rj.wisp.R;
import com.rj.wisp.core.WispCore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 定制化 设置项 属性
 */
public class PhoneSettingAdapter extends SimpleAdapter {

    private EditText portEditText = null;
    private EditText hostEditText = null;
    private EditText vpnSelectET = null;
    private EditText vpnHostET = null;
    private EditText vpnPortET = null;
    private EditText vpnNameET = null;
    private EditText vpnPasswordET = null;
    private EditText appSelectEditt = null;

    private Context context;
    private AppSettingCallBack appSettingCallBack;

    public interface AppSettingCallBack {
        void callBack(EditText editText, String jsonStr);

        void getAppInfo(View v, String securityHost, int securityPort);
    }

    public void saveConfig() {
        try {
            if (vpnNameET != null) {
                DB.VPN_HOST = vpnHostET.getText().toString();
                DB.VPN_PORT = Integer.valueOf(vpnPortET.getText().toString());
                DB.VPN_USER = vpnNameET.getText().toString();
                DB.VPN_PASS = vpnPasswordET.getText().toString();
            }

            DB.SECURITY_HOST = hostEditText.getText() + "";
            DB.SECURITY_PORT = Integer.parseInt(portEditText.getText()
                    + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PhoneSettingAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource, String[] from,
                               int[] to, AppSettingCallBack appSettingCallBack) {
        super(context, data, resource, from, to);
        this.context = context;
        this.appSettingCallBack = appSettingCallBack;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 针对不同 按钮做出逻辑处理
        HashMap<String, String> map = (HashMap<String, String>) this
                .getItem(position);
        View view = super.getView(position, convertView, parent);
        if ("应用地址:".equals(map.get("item"))) {
            hostEditText = (EditText) view.findViewById(R.id.item_edit);
            if (!DB.CHANGE_HOST) {
                hostEditText.setEnabled(false);
            }
            // hostEditText.setText(DB.SECURITY_HOST);
        } else if ("平台端口:".equals(map.get("item"))) {
            portEditText = (EditText) view.findViewById(R.id.item_edit);
            if (!DB.CHANGE_HOST) {
                portEditText.setEnabled(false);
            }
            //portEditText.setText(DB.SECURITY_PORT);
        } else if ("是否使用VPN拨号:".equals(map.get("item"))) {
            vpnSelectET = (EditText) view.findViewById(R.id.item_edit);
            vpnSelectET.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.mipmap.down_arrow), null);

            vpnSelectET.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    appSettingCallBack.callBack(vpnSelectET, "");
                }
            });
        } else if ("VPN地址:".equals(map.get("item"))) {
            vpnHostET = (EditText) view.findViewById(R.id.item_edit);
        } else if ("VPN端口:".equals(map.get("item"))) {
            vpnPortET = (EditText) view.findViewById(R.id.item_edit);
        } else if ("VPN帐号:".equals(map.get("item"))) {
            vpnNameET = (EditText) view.findViewById(R.id.item_edit);
//			vpnNameET.setText("sangfor");
//			vpnNameET.setOnFocusChangeListener(new OnFocusChangeListener() {
//				
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					DB.VPN_USER = vpnNameET.getText().toString();
//				}
//			}); 
        } else if ("VPN密码:".equals(map.get("item"))) {
            vpnPasswordET = (EditText) view.findViewById(R.id.item_edit);
//			vpnPasswordET.setText("admin");
            vpnPasswordET.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
//			vpnPasswordET.setOnFocusChangeListener(new OnFocusChangeListener() {
//				
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					DB.VPN_PASS = vpnPasswordET.getText().toString();
//				}
//			}); 
        } else if ("应用选择:".equals(map.get("item"))) {
            appSelectEditt = (EditText) view.findViewById(R.id.item_edit);
            appSelectEditt.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.mipmap.down_arrow), null);
            if (!DB.CHANGE_HOST) {
                appSelectEditt.setEnabled(false);
            }
            appSelectEditt.setFocusable(false);
            appSelectEditt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String secHost = hostEditText.getText().toString();
                        int secPort = Integer.valueOf(portEditText.getText().toString());
                        if (appSettingCallBack != null) {
                            appSettingCallBack.getAppInfo(v, secHost, secPort);
                        }
                    } catch (NumberFormatException e) {
                        ToastTool.show(context, "端口号格式不正确", Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }

                }
            });

//            appSelect(view);
        } else if ("重置程序".equals(map.get("item"))) {
        } else if ("缓存设置".equals(map.get("item"))) {
        } else if ("关于我们".equals(map.get("item"))) {
        } else if ("检查更新".equals(map.get("item"))) {
        } else if ("问题或意见反馈".equals(map.get("item"))) {
        } else if ("帮助".equals(map.get("item"))) {
        } else if ("APN选择:".equals(map.get("item"))) {
        } else if ("问题或者意见反馈".equals(map.get("item"))) {
        } else if ("使用条款和隐私政策".equals(map.get("item"))) {
        } else if ("版本号".equals(map.get("item"))) {
            view.findViewById(
                    view.getContext()
                            .getResources()
                            .getIdentifier("imageView1", "id",
                                    view.getContext().getPackageName()))
                    .setVisibility(View.GONE);
            TextView versionTxt = (TextView) view.findViewById(view
                    .getContext()
                    .getResources()
                    .getIdentifier("item_content", "id",
                            view.getContext().getPackageName()));
            versionTxt.setText(map.get("version"));
            versionTxt.setVisibility(View.VISIBLE);
        }

        return view;
    }

    // 应用选择
    public void appSelect(View view) {
        final EditText editText = (EditText) view.findViewById(R.id.item_edit);
        editText.setFocusable(false);
        editText.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //重启新的HTTP Server监听
                WispCore.getWISPSO().CloseService();
                WispCore.getWISPSO().StartService(new Handler(), context);

                // 回调到activity处理
//				final GetAppConfigTask getAppConfigTask = new GetAppConfigTask(
//						activity, new GetAppConfigCallBack() {
//							@Override
//							public void callback(String jsonStr) {
//								appSettingCallBack.callBack(editText, jsonStr);
//							}
//						});

                DB.SECURITY_HOST = hostEditText.getText() + "";

                // 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                try {
                    DB.SECURITY_PORT = Integer.parseInt(portEditText.getText()
                            + "");
                } catch (Exception e) {
                    DB.SECURITY_PORT = 0;
                }
//				final GetAppConfigDialog appLoadDialog = new GetAppConfigDialog(
//						v.getContext());
//				appLoadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//				appLoadDialog.setTitle("");
//				appLoadDialog.setMessage("正在获取应用列表...");
//				appLoadDialog.setOnCancelListener(new OnCancelListener() {
//					@Override
//					public void onCancel(DialogInterface dialog) {
//						getAppConfigTask.cancel(true);
//
//					}
//				});
//				getAppConfigTask.execute();
            }
        });
    }


}
