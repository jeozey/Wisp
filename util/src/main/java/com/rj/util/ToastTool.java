package com.rj.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.rj.view.msg.AppMsg;

import static android.view.Gravity.BOTTOM;
import static com.rj.view.msg.AppMsg.LENGTH_LONG;
import static com.rj.view.msg.AppMsg.LENGTH_SHORT;

/**
 * 作者：志文 on 2015/11/20 0020 09:56
 * 邮箱：594485991@qq.com
 */
public class ToastTool {

    public static void show(Context context, String msg, int time) {
        try {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                AppMsg.Style style = AppMsg.STYLE_INFO;
                AppMsg appMsg = null;
                if (Toast.LENGTH_LONG == time) {
                    style = new AppMsg.Style(LENGTH_LONG, R.color.custom);
                } else {
                    style = new AppMsg.Style(LENGTH_SHORT, R.color.custom);
                }
                appMsg = AppMsg.makeText(activity, msg, style);
                appMsg.setAnimation(R.anim.slide_in_up, R.anim.slide_out_down);
                appMsg.setLayoutGravity(BOTTOM);
                appMsg.show();
            } else {
                Toast.makeText(context, msg, time).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
