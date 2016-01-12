package com.rj.sdkey.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.icloudaegis.service.KeySdkService;
import com.icloudaegis.service.PinResult;
import com.rj.sdkey.R;
import com.rj.view.ToastTool;

public class PhoneLoginView extends LinearLayout implements OnClickListener {

    public interface IKeySdkService {

        void loginSuccess();

        void exit();

    }

    private RelativeLayout pinLoginView;
    private RelativeLayout pinChangeView;
    private LayoutInflater inflater = null;
    private KeySdkService keySdkService;
    private IKeySdkService iKeySdkService;
    private EditText pinLoginEdText;
    private EditText pinChangeEdText;
    private EditText pinNewEdText;
    private EditText pinAganewEdText;
    private ImageButton pinLoginBtn;
    private ImageButton pinExitBtn;
    private ImageButton pinChangeBtn;
    private Button pinEnsureBtn;
    private Button pinCancleBtn;
    private static final long PinRight = 0;
    private static final long PinError = -1;
    private static final long initError = -1;
    private String string;

    public PhoneLoginView(Context context, IKeySdkService iKeySdkService) {
        super(context);
        this.keySdkService = new KeySdkService();
        this.iKeySdkService = iKeySdkService;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View login = inflater.inflate(R.layout.phone_login_view, null);
        addView(login, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        View change = inflater.inflate(R.layout.phone_change_view, null);
        addView(change, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        pinLoginBtn = (ImageButton) login.findViewById(R.id.login_btn);
        pinChangeBtn = (ImageButton) login.findViewById(R.id.change_btn);
        pinExitBtn = (ImageButton) login.findViewById(R.id.exit_btn);
        pinEnsureBtn = (Button) change.findViewById(R.id.ensure_btn);
        pinCancleBtn = (Button) change.findViewById(R.id.cancle_btn);

        pinLoginEdText = (EditText) login.findViewById(R.id.pin_login_edt);
        pinChangeEdText = (EditText) change.findViewById(R.id.pin_change_edt);
        pinNewEdText = (EditText) change.findViewById(R.id.pin_new_edt);
        pinAganewEdText = (EditText) change.findViewById(R.id.pin_aganew_edt);

        pinLoginView = (RelativeLayout) findViewById(R.id.login_view);
        pinChangeView = (RelativeLayout) findViewById(R.id.change_view);

        pinLoginBtn.setOnClickListener(this);
        pinExitBtn.setOnClickListener(this);
        pinChangeBtn.setOnClickListener(this);
        pinEnsureBtn.setOnClickListener(this);
        pinCancleBtn.setOnClickListener(this);

        // 点击EditText其他位置关闭软键盘的响应事件
        pinLoginView.setOnClickListener(this);
        pinChangeView.setOnClickListener(this);

        // 登入界面软键盘"完成"的监听
        pinLoginEdText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_DONE) {
                    loginPin();
                }
                return false;
            }
        });

        // 修改界面软键盘"完成"的监听
        pinAganewEdText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_DONE) {
                    changePin();
                }
                return false;
            }
        });

    }

    // SDKEY的初始化
    public void initPin(Context context) {
        // 初始化SDKEY
        PinResult result = keySdkService.initialize(context);
        // -1初始化失败，0初始化成功
        if (result.getResult() == initError) {
            string = "请确认SDKEY有插入";
        } else {
            string = "SDKEY初始化成功";
        }
        ToastTool.show(getContext(), string, Toast.LENGTH_SHORT);
    }

    // 登入按钮的逻辑判断
    private void loginPin() {
        final String pin = pinLoginEdText.getText().toString().trim();
        if (!keySdkService.isInit()) {
            ToastTool.show(getContext(), "请确认SDKEY有插入", Toast.LENGTH_SHORT);
        } else {
            if (TextUtils.isEmpty(pin)) {
                ToastTool.show(getContext(), "输入PIN码不能为空", Toast.LENGTH_SHORT);
            } else if (pin.length() < 6 || pin.length() > 16) {
                ToastTool.show(getContext(), "PIN码长度为6-16位", Toast.LENGTH_SHORT);
            } else {
                keySdkService.unblockPIN("11111111", "111111");
                PinResult ret = keySdkService.loginPIN(pin);
                if (ret.getResult() == PinRight) {
                    ToastTool.show(getContext(), "登入成功", Toast.LENGTH_SHORT);
                    iKeySdkService.loginSuccess();
                } else if (keySdkService.ulRetrycount() == 0) {
                    ToastTool.show(getContext(), "SDKEY已锁定,请联系管理员解锁！", Toast.LENGTH_SHORT);
                } else if (ret.getResult() == PinError) {
                    // 返回一个ulRetrycount错误的次数
                    int count = keySdkService.ulRetrycount();
                    // 输错5次count[0]等于0，sdkey锁住
                    if (count <= 0) {
                        ToastTool.show(getContext(), "SDKEY已锁定,请联系管理员解锁！", Toast.LENGTH_SHORT);
                    } else {
                        ToastTool.show(getContext(), "登入失败,Pin码错误！您还有" + count + "次机会", Toast.LENGTH_SHORT);
                    }
                }
            }
        }
    }

    // 修改Pin的逻辑判断
    private void changePin() {
        final String oldpin = pinChangeEdText.getText().toString().trim();
        final String newpin = pinNewEdText.getText().toString().trim();
        final String agnewpin = pinAganewEdText.getText().toString().trim();
        // 先判断SDKEY是否加载成功
        if (!keySdkService.isInit()) {
            string = "请确认SDKEY有插入";
        } else if (TextUtils.isEmpty(oldpin) || TextUtils.isEmpty(newpin)
                || TextUtils.isEmpty(agnewpin)) {
            string = "输入PIN码不能为空";
        } else if (oldpin.length() < 6 || oldpin.length() > 16
                || newpin.length() < 6 || newpin.length() > 16) {
            string = "PIN码长度为6-16位";
        } else if (newpin.equals(agnewpin)) {
            PinResult ret = keySdkService.changePIN(oldpin, agnewpin);
            int count = keySdkService.ulRetrycount();
            if (ret.getResult() == PinRight) {
                string = "修改PIN码成功";
                pinLoginView.setVisibility(View.VISIBLE);
                pinChangeView.setVisibility(View.GONE);
                pinChangeEdText.setText("");
                pinNewEdText.setText("");
                pinAganewEdText.setText("");
            } else if (count == 0) {
                string = "SDKEY已锁定,请联系管理员解锁！";
            } else if (ret.getResult() == PinError && count != 0) {
                string = "修改失败,输入旧PIN码错误！" + "您还有" + count + "次机会！";
            }
        } else if (!newpin.equals(agnewpin)) {
            string = "输入的新PIN码不一致";
        }
        ToastTool.show(getContext(), string, Toast.LENGTH_SHORT);
    }

    // 按钮的响应事件
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.login_btn) {
            loginPin();
        } else if (id == R.id.exit_btn) {
            iKeySdkService.exit();
        } else if (id == R.id.ensure_btn) {
            changePin();
        } else if (id == R.id.cancle_btn) {
            pinLoginView.setVisibility(View.VISIBLE);
            pinChangeView.setVisibility(View.GONE);
            pinChangeEdText.setText("");
            pinNewEdText.setText("");
            pinAganewEdText.setText("");
            // 打开软件盘
            InputMethodManager imm_login = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm_login.showSoftInput(pinLoginEdText,
                    InputMethodManager.SHOW_FORCED);
        } else if (id == R.id.change_btn) {
            pinLoginView.setVisibility(View.GONE);
            pinChangeView.setVisibility(View.VISIBLE);
            pinLoginEdText.setText("");
            // 打开软件盘
            InputMethodManager imm_change = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm_change.showSoftInput(pinChangeEdText,
                    InputMethodManager.SHOW_FORCED);
        } else if (id == R.id.login_view) {
            // 关闭软键盘
            InputMethodManager login_imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            login_imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } else if (id == R.id.change_view) {
            // 关闭软键盘
            InputMethodManager change_imm = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            change_imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}

