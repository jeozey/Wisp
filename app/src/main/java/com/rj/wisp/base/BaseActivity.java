package com.rj.wisp.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.rj.view.TabMenu;
import com.rj.wisp.R;


public class BaseActivity extends Activity {
    private static final String TAG = BaseActivity.class.getName();

    private TabMenu.MenuBodyAdapter bodyAdapter = new TabMenu.MenuBodyAdapter(
            this, new int[]{R.mipmap.menu_fresh, R.mipmap.menu_reset,
            R.mipmap.menu_setting, R.mipmap.menu_exit}, new String[]{"刷新", "重置", "设置", "退出"});
    protected TabMenu tabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        tabMenu = new TabMenu(this, new BodyClickEvent(), 0);// 出现与消失的动画
        tabMenu.update();
        tabMenu.SetBodyAdapter(bodyAdapter);
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

    protected void onTapMenuSelect(int position) {

    }

    @Override
    /**
     * 创建MENU
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");// 必须创建一项
        return super.onCreateOptionsMenu(menu);
    }


    public void clearCache(WebView mWebView, boolean disk) {
        if (mWebView != null) {
            mWebView.clearCache(disk);
        }
    }

    public void clearCookies() {
        CookieManager c = CookieManager.getInstance();
        c.removeAllCookies(null);
    }


    /**
     * 显示配置页面
     */
    private void showSettingDialog(String type) {

        try {
            Log.e("NNN", "showSettingDialog");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        // 退出程序，而不是退出到AppLoadActivity
        exitApp();
    }

    public void exitApp() {
//		AlertDialog.Builder alertbBuilder = new AlertDialog.Builder(this);
//
//		alertbBuilder.setTitle("系统提示").setMessage("您确定退出程序吗?")
//				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.cancel();
//					}
//				})
//				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						// 取消网络监听悬浮窗
//						Intent exipAppIntent = new Intent(BaseActivity.this,
//								NetConnectService.class);
//						exipAppIntent.putExtra("FLAG_IS_EXIT", true);
//						startService(exipAppIntent);
//
//						// 停止8011监听服务
//						WISP.getWISPSO().CloseService();
//						// 删除临时文件
//						FileUtil.deleteTempFile();
//
//						// 退出VPN
//						if (DB.USE_VPN) {
//							SangforAuth.getInstance().vpnLogout();
//						}
//
//						new Handler().postDelayed(new Runnable() {
//
//							@Override
//							public void run() {
//								Intent intent = new Intent(Intent.ACTION_MAIN);
//								intent.addCategory(Intent.CATEGORY_HOME);
//								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//								startActivity(intent);
//								android.os.Process
//										.killProcess(android.os.Process.myPid());
//							}
//						}, 1000);
//						Log.i("GuLang", "BaseActivity exitapp");
//						// Intent exitAppIntent = new Intent(
//						// "com.rj.wisp.exitapp");
//						// sendBroadcast(exitAppIntent);
//					};
//				}).create();
//
//		alertbBuilder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "BaseActivity->onDestroy");
        super.onDestroy();
    }
}
