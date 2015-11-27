package com.rj.framework;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rj.util.PixelTool;
import com.rj.view.button.CustomButton;

public class ButtonFactory {

    private static String filepath = DB.RESOURCE_PATH;

    public static RelativeLayout getBottomButtonAndText2(
            final CustomButton button, Context context, int hpix, int wpix,
            final TextView view) {

        RelativeLayout relativeLayout = new RelativeLayout(context);
        LinearLayout.LayoutParams bottomLayParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        bottomLayParams.topMargin = 2;
        // bottomLayParams.rightMargin=5;
        // bottomLayParams.leftMargin=5;

        relativeLayout.setLayoutParams(bottomLayParams);
        LinearLayout linearLayout = getBottomButtonAndText(button, context,
                hpix, wpix, view);
        linearLayout.setContentDescription(button.getButtontext());
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        btnParams.bottomMargin = -2;
        // btnParams.topMargin=10;

        linearLayout.setLayoutParams(btnParams);
        relativeLayout.addView(linearLayout);
        RelativeLayout.LayoutParams numParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // relativeLayout.getLayoutParams().width=relativeLayout.getMeasuredWidth();

        // AutoSizeTool.setBottomNavigationButtonNumParams(numParams);
        relativeLayout.addView(getButtonNum(context, hpix, wpix), numParams);
        // if("true".equals(button.getIsclick().toLowerCase())) {
        // relativeLayout.setBackgroundResource(
        // relativeLayout.getContext().getResources().getIdentifier("menuicobg",
        // "drawable", relativeLayout.getContext().getPackageName()));
        // WebMainActivity.currentMenuBtn = relativeLayout;
        // WebMainActivity.currentWebView.loadUrl(button.getClickevent());
        // }

        relativeLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
        return relativeLayout;
    }

    public static TextView getButtonNum(Context context, int hpix, int wpix) {
        TextView textView = new TextView(context);

        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setBackgroundResource(context.getResources().getIdentifier(
                "numbg", "drawable", context.getPackageName()));
        // AutoSizeTool.setBottomNavigationButtonNum(textView);
        TextPaint tpaint = textView.getPaint();
        tpaint.setFakeBoldText(true);
        // Log.e("wufeng", "!!!!!!!!!"+ PixelTool.px2dip(context,
        // textView.getTextSize()));
        // LayoutParams layoutParams = new LayoutParams(5, 5);
        // textView.setLayoutParams(layoutParams);
        textView.setVisibility(View.GONE);
        textView.bringToFront();
        return textView;
    }

    public static LinearLayout getBottomButtonAndText(
            final CustomButton button, Context context, int hpix, int wpix,
            final TextView view) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.addView(getBottomButton(button.getBeforeimg(),
                button.getAfterimg(), button.getClickevent(), context, hpix,
                wpix));
        linearLayout.addView(getBottomButtonText(button.getButtontext(),
                context, hpix, wpix));
        return linearLayout;
    }

    public static LinearLayout getBottomButtonAndText(CustomButton button,
                                                      Context context, int hpix, int wpix) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        linearLayout.addView(getBottomButton(button.getBeforeimg(),
                button.getAfterimg(), button.getClickevent(), context, hpix,
                wpix));
        linearLayout.addView(getBottomButtonText(button.getButtontext(),
                context, hpix, wpix));

        return linearLayout;
    }

    public static TextView getBottomButtonText(String buttonText,
                                               Context context, int hpix, int wpix) {
        TextView textView = new TextView(context);
        textView.setText(buttonText);
        // int color =getResources().getIdentifier("notice_color", "color",
        // getPackageName());
        // textView.setTextColor(color); // size
        // textView.setTextSize(hpix / 85);

        textView.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = -5;
        // lp.bottomMargin=3;
        textView.setLayoutParams(lp);
        // AutoSizeTool.setBottomNavigationButtonText(textView);
        return textView;
    }

    public static TextView getBottomButton(String beforeImg, String afterImg,
                                           final String clickEvent, Context context, int hpix, int wpix) {
        TextView textView = new TextView(context);

        Drawable drawable = null;
        try {
            drawable = Drawable.createFromPath(DB.RESOURCE_PATH
                    + beforeImg);
        } catch (Exception e) {
            e.printStackTrace();
            drawable = context.getResources().getDrawable(
                    context.getResources().getIdentifier("dbicon72",
                            "drawable", context.getPackageName()));
            Log.e("mmm", "出错");
        }

        textView.setBackgroundDrawable(drawable);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(hpix / 15,
                hpix / 15);
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        textView.setLayoutParams(lp);

        // AutoSizeTool.setBottomNavigationButton(textView,
        // AutoSizeTool.TYPE_LINEARLAYOUT);
        return textView;

    }

    public static Drawable getDrawable(Context context, String beforeImg) {

        Drawable drawable = null;
        try {
            drawable = Drawable.createFromPath(filepath + beforeImg);
            if (drawable == null) {
                drawable = context.getResources().getDrawable(
                        R.drawable.dbicon72);
            }
        } catch (Exception e) {
            e.printStackTrace();
            drawable = context.getResources().getDrawable(R.drawable.dbicon72);
            Log.e("mmm", "出错");
        }

        return drawable;

    }

    public static LinearLayout getNotice(String text, String checkEvent,
                                         Context context, int hpix, int wpix, View.OnClickListener listener) {
        LinearLayout linearLayout = getBottomLinearLayout(context, hpix, wpix);
        // linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.addView(getNoticeText(text, checkEvent, context, hpix,
                wpix, listener));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(params);
        return linearLayout;
    }

    public static LinearLayout getBottomLinearLayout(Context context, int hpix,
                                                     int wpix) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams lp = new LayoutParams(wpix, PixelTool.dip2px(context, 50));
        linearLayout.setLayoutParams(lp);
        return linearLayout;
    }

    public static TextView getNoticeText(String text, final String checkEvent,
                                         Context context, int hpix, int wpix, View.OnClickListener listener) {
        TextView textView = new TextView(context);
        Log.e("test9", "hpix:" + hpix);

        if (hpix > 1500) {
            textView.setTextSize(context.getResources().getDimension(
                    R.dimen.sp5));
        } else {
            textView.setTextSize(hpix / 50);
        }
        textView.setSingleLine(true);
        textView.setText(text + "      ");
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER);

        textView.setOnClickListener(listener);
        return textView;
    }

}
