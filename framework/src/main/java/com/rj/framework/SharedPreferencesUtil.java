package com.rj.framework;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    private static SharedPreferences sharedPreferences;


    public static SharedPreferences getSharedPreferences(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getPackageName(),
                    Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }
}
