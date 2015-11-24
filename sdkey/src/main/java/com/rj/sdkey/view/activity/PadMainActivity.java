package com.rj.sdkey.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.LinearLayout.LayoutParams;

import com.rj.sdkey.view.PadLoginView;


public class PadMainActivity extends Activity implements PadLoginView.IKeySdkService {

    private PadLoginView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //添加布局文件
        initView();
        //初始化，识别SDKEY
        login.initPin(getBaseContext());
    }

    private void initView() {

        LayoutParams lParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        login = new PadLoginView(getBaseContext(), this);
        addContentView(login, lParams);
    }


    // 登入成功
    @Override
    public void loginSuccess() {

    }

    // 退出程序
    @Override
    public void exit() {
        finish();
    }

    // 监听返回键
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
