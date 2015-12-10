package com.rj.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * 作者：志文 on 2015/12/10 0010 15:19
 * 邮箱：594485991@qq.com
 */
public class SystemUtil {
    //判断是否为平板
    public static boolean isPad(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        //屏幕宽高
//        float screenWidth = display.getWidth();
//        float screenHeight = display.getHeight();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        //屏幕尺寸
        double screenInches = Math.sqrt(x + y);
        //大于6尺寸则判定为pad
        return screenInches >= 6.0;
    }
}
