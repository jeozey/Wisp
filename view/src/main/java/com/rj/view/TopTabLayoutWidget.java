package com.rj.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.rj.view.button.CustomWidgetButton;

import java.util.List;

public class TopTabLayoutWidget extends LinearLayout implements OnClickListener {
    private static final String TAG = "TabLayoutWidget";
    private RadioGroup parent;

    public interface ITabLayoutWidget {
        void callBack(String callBack);
    }

    private Context context;
    private ITabLayoutWidget iLayoutWidget;
    private List<CustomWidgetButton> buttons;
    private int lastIndex = 0;
    private View contentView;

    public TopTabLayoutWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public TopTabLayoutWidget(Context context) {
        super(context);
        this.context = context;
    }

    public void init(final int metricsWidth,
                     final List<CustomWidgetButton> buttons,
                     final ITabLayoutWidget iLayoutWidget) {
        removeAllViews();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = inflater.inflate(R.layout.top_tab_widget_layout, null);
        addView(contentView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        parent = (RadioGroup) contentView.findViewById(R.id.parentRadioGroup);
        initializeTabs(metricsWidth, contentView, buttons, iLayoutWidget);
    }

    private void initializeTabs(int metricsWidth, View view,
                                final List<CustomWidgetButton> buttons,
                                final ITabLayoutWidget iLayoutWidget) {
        this.buttons = buttons;
        this.iLayoutWidget = iLayoutWidget;
        int size = buttons.size();
        Log.e(TAG, "initializeTabs:" + size);
        if (size == 0) {
            contentView.setVisibility(View.GONE);
        }

        for (CustomWidgetButton customWidgetButton : buttons) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(customWidgetButton.getTitle());
            radioButton.setOnClickListener(TopTabLayoutWidget.this);
            radioButton.setTag(customWidgetButton.getCallBack());

            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            radioButton.setPadding(
                    (int) context.getResources().getDimension(
                            R.dimen.radio_button_margin_left_right),
                    (int) context.getResources().getDimension(
                            R.dimen.radio_button_margin_top_bottom),
                    (int) context.getResources().getDimension(
                            R.dimen.radio_button_margin_left_right),
                    (int) context.getResources().getDimension(
                            R.dimen.radio_button_margin_top_bottom));
            radioButton.setBackgroundResource(R.drawable.radiobtn_selector);
            radioButton.setLayoutParams(params);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setButtonDrawable(android.R.color.transparent);
            if (size < 4) {
                radioButton.setTextSize(context.getResources().getDimension(
                        R.dimen.radio_button_txt_size2));
            } else if (size > 4) {
                Log.e(TAG, "here");
                radioButton.setTextSize(context.getResources().getDimension(
                        R.dimen.radio_button_txt_size0));
                radioButton.setPadding(
                        (int) context.getResources().getDimension(
                                R.dimen.radio_button_margin_left_right1),
                        (int) context.getResources().getDimension(
                                R.dimen.radio_button_margin_top_bottom1),
                        (int) context.getResources().getDimension(
                                R.dimen.radio_button_margin_left_right1),
                        (int) context.getResources().getDimension(
                                R.dimen.radio_button_margin_top_bottom1));
            }
            radioButton.setCompoundDrawables(null, null, null, null);
            radioButton.setTextColor(Color.WHITE);
            parent.addView(radioButton);

            if ("true".equals(customWidgetButton.getIsclick())) {
                Log.e(TAG, "TAB默认点击项:" + customWidgetButton);
                String callBack = customWidgetButton.getCallBack();
                tabEvent(callBack);
                radioButton.setChecked(true);
                radioButton.setTextColor(context.getResources().getColor(
                        R.color.theme));
                if (oldRadioButton != null&&!oldRadioButton.getText().equals(radioButton.getText()))
                    radioButton.setTextColor(Color.WHITE);
                oldRadioButton = radioButton;
                // parent.check(radioButton.getId());
            }
        }

    }

    // 清除tab标签栏
    public void clear() {
        Log.i(TAG, "clear()");
        try {
            if (parent != null) {
                parent.setVisibility(View.GONE);
            }
            requestLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RadioButton oldRadioButton = null;

    @Override
    public void onClick(View v) {
        try {
            RadioButton a = (RadioButton) v;
            if (oldRadioButton != null) {
                oldRadioButton.setTextColor(Color.WHITE);
            }
            a.setTextColor(context.getResources().getColor(R.color.theme));
            tabEvent(v.getTag());
            oldRadioButton = a;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void tabEvent(Object callBack) {
        try {
            Log.e(TAG, "callBack:" + callBack);
            iLayoutWidget.callBack(callBack.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
