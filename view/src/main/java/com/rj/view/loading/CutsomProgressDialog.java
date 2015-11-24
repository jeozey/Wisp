package com.rj.view.loading;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rj.view.R;

public class CutsomProgressDialog {
    private static final String TAG = "CutsomProgressDialog";
    public boolean isShowDialog = false;

    public RelativeLayout dialogView = null;
    public ImageView dialogImageView = null;
    private Animation hyperspaceJumpAnimation;
    private Context context;
    private int width;
    private View parent;

    // parent依附类必须为FrameLayout
    public CutsomProgressDialog(Context context, int width, View parent) {
        Log.e(TAG, "width:" + width);
        this.context = context;
        this.width = width;
        this.parent = parent;
    }

    public boolean isShowing() {
        return isShowDialog;
    }

    // 转圈圈
    public void show() {
        try {
            Log.e(TAG, "showDialogView...");
            if (dialogView == null) {
                dialogImageView = new ImageView(context);
//				dialogImageView.setId(20121223);
                LinearLayout lineLayout = new LinearLayout(context);
                lineLayout.setBackgroundColor(Color.parseColor("#000000"));
                lineLayout.getBackground().setAlpha(80);
                lineLayout.setOrientation(LinearLayout.VERTICAL);
                lineLayout.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                dialogImageView.setLayoutParams(layoutParams);
                lineLayout.addView(dialogImageView);
                dialogImageView.setBackgroundDrawable(context.getResources().getDrawable(
                        R.mipmap.load11));
                hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,
                        R.anim.load);


                TextView textView = new TextView(context);
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams2.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                layoutParams2.leftMargin = 15;
                layoutParams2.rightMargin = 15;
                textView.setLayoutParams(layoutParams2);
                textView.setText("加载中...");
                // textView.setTextColor(Color.BLUE);
                lineLayout.addView(textView);

                dialogView = new RelativeLayout(context);
                dialogView.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;// 截取手势，让挡板后面的webview不能被点击
                    }
                });
                RelativeLayout.LayoutParams layoutpare = new RelativeLayout.LayoutParams(
                        width / 10, width / 10);
                layoutpare.addRule(RelativeLayout.CENTER_IN_PARENT);
                dialogView.addView(lineLayout, layoutpare);

                if (dialogView.getParent() != null) {
                    ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                }
                dialogView.setBackgroundColor(Color.parseColor("#000000"));
                dialogView.getBackground().setAlpha(100);


            }
            dialogImageView.startAnimation(hyperspaceJumpAnimation);
            // RelativeLayout.LayoutParams lp = new
            // RelativeLayout.LayoutParams(
            // width/200,
            // width/200);
            dialogView.setVisibility(parent.getVisibility());
            // dialogView.getChildAt(0).setVisibility(parent.getVisibility());
            // ((ViewGroup) parent).addView(dialogView, lp);
            parent.requestLayout();
            parent.invalidate();
            // ((ViewGroup) parent).addView(dialogView, 0, lp);
            ((ViewGroup) parent).addView(dialogView);
            // ((ViewGroup) parent).addView(dialogView, lp);
            isShowDialog = true;

        } catch (Exception e) {
            Log.e(TAG, "err:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void dismiss() {
        try {
            Log.e(TAG, "dismiss...");
            if (dialogView != null) {
                if (dialogView.getParent() != null) {
                    ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                }
            }
            isShowDialog = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
