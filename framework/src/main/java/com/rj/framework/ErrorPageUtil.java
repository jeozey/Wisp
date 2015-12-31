package com.rj.framework;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * 作者：志文 on 2015/12/29 0029 14:25
 * 邮箱：594485991@qq.com
 */
public class ErrorPageUtil {
    private static final String TAG = ErrorPageUtil.class.getName();
    private static String errorPageHead = "";
    private static String errorPageEnd = "";
    private static String errorPage = "";
    private static StringBuilder errorPageSB;

    public static String getErrorPage(Context context, String errUrl) {
        try {
            if (TextUtils.isEmpty(errorPage)) {
                errorPageSB = new StringBuilder();
                AssetManager assetManager = context.getAssets();
                InputStream is = assetManager.open("errorPage.html");
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] b = new byte[1024];
                int l = 0;
                while ((l = is.read(b)) != -1) {
                    os.write(b, 0, l);
                }
                os.close();
                is.close();
                errorPage = os.toString();
                int i = errorPage.indexOf("@");
                errorPageHead = errorPage.substring(0, i);
                errorPageSB.append(errorPageHead);
                errorPageEnd = errorPage.substring(i);

            }
            errorPageSB.append(errUrl);
            errorPageSB.append(errorPageEnd);
            return errorPageSB.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
