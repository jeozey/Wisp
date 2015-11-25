package com.rj.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.rj.view.msg.AppMsg;

import java.lang.reflect.Field;
import java.util.HashMap;

import static android.view.Gravity.BOTTOM;
import static com.rj.view.msg.AppMsg.LENGTH_LONG;
import static com.rj.view.msg.AppMsg.LENGTH_SHORT;

/**
 * 作者：志文 on 2015/11/20 0020 09:56
 * 邮箱：594485991@qq.com
 */
public class ToastTool {
    public static Activity getActivity() throws Exception {
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
        activitiesField.setAccessible(true);
        HashMap activities = (HashMap) activitiesField.get(activityThread);
        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = activityRecordClass.getDeclaredField("paused");
            pausedField.setAccessible(true);
            if (!pausedField.getBoolean(activityRecord)) {
                Field activityField = activityRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                return activity;
            }
        }
        return null;
    }

    public static void show(Context context, String msg, int time) {
        try {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                showToast(activity, msg, time);
            } else {
//                showToast(context.getApplicationContext(), msg, time);
                try {
                    Activity activity = getActivity();
                    showToast(activity, msg, time);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, msg, time).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showToast(Activity activity, String msg, int time) {
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
    }
}
