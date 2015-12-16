package com.rj.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 作者：志文 on 2015/12/15 0015 09:23
 * 邮箱：594485991@qq.com
 */
public class BackTitleBar extends RelativeLayout {
    private View view;
    private TextView leftBtn, title, rightBtn;

    public BackTitleBar(Context context) {
        super(context);
    }

    public BackTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //在构造函数中将Xml中定义的布局解析出来。
        view = LayoutInflater.from(context).inflate(R.layout.back_title_bar, this, true);
        initView();
    }

    public BackTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        view = LayoutInflater.from(context).inflate(R.layout.back_title_bar, this, true);
        initView();
    }

    private void initView() {
        leftBtn = (TextView) view.findViewById(R.id.leftBtn);
        title = (TextView) view.findViewById(R.id.title);
        rightBtn = (TextView) view.findViewById(R.id.rightBtn);
    }

    public void setBtnOnclickListener(View.OnClickListener listener) {
        if (listener != null && leftBtn != null && rightBtn != null) {
            leftBtn.setOnClickListener(listener);
            rightBtn.setOnClickListener(listener);
        }
    }

    public void setLeftText(String txt) {
        leftBtn.setText(txt);
        leftBtn.setVisibility(View.VISIBLE);
    }

    public void setRightText(String txt) {
        rightBtn.setText(txt);
        rightBtn.setVisibility(View.VISIBLE);
    }

    public void setTitle(String txt) {
        title.setText(txt);
    }

}
