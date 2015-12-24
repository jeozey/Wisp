package com.rj.framework;

import android.app.Notification;
import android.content.Context;

/**
 * 作者：志文 on 2015/12/23 0023 22:03
 * 邮箱：594485991@qq.com
 */
public class NotificationUtil {
    private static final String TAG = NotificationUtil.class.getName();

    public static Notification getNotification(Context context, String title, int smallIcon) {
        return new Notification.Builder(context).setContentTitle(title).setSmallIcon(smallIcon).build();
    }
}
