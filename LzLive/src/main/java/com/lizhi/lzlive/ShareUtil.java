package com.lizhi.lzlive;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;


import java.lang.ref.SoftReference;

public class ShareUtil {

    public static final String PHONE_NUM = "PHONE_NUM";

    private static Context app;

    public static void init(Context context) {
        app = context;
    }

    private static SoftReference<SharedPreferences> mReference;

    private static SharedPreferences getSharedPreferences() {
        if (app != null &&(mReference == null || mReference.get() == null)) {
            SharedPreferences preferences = app.getSharedPreferences(
                    app.getPackageName() + "_live", Context.MODE_PRIVATE);
            mReference = new SoftReference<>(preferences);
            return preferences;
        }

        return mReference.get();
    }

    private static SharedPreferences.Editor getEditor() {
        return getSharedPreferences().edit();
    }

    public static String get(String key, String defValue) {
        return getSharedPreferences().getString(key, defValue);
    }

    public static void put(String key, String value) {
        getSharedPreferences().edit().putString(key, value).apply();
    }

    public static boolean get(String key, boolean defValue) {
        return getSharedPreferences().getBoolean(key, defValue);
    }

    public static void put(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).apply();
    }
}
