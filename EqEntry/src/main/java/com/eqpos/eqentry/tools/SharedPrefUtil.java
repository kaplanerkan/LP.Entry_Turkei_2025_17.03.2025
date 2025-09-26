package com.eqpos.eqentry.tools;

import static android.content.pm.PackageManager.GET_META_DATA;
import static android.os.Build.VERSION_CODES.P;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

public class SharedPrefUtil {

    public static final String KEY_BLE_PRINTER_STATUS = "KEY_BLE_PRINTER_STATUS";
    public static final String KEY_VARYANT_SATIS_FIYATI = "KEY_VARYANT_SATIS_FIYATI";
    public static final String KEY_SELECTED_DEPO_ID = "KEY_SELECTED_DEPO_ID";

    private static final String PREFER_NAME = "Reg";
    private static SharedPrefUtil instance;
    private static SharedPreferences.Editor editor;
    private static SharedPreferences pref;

    public static void init(Context context) {
        pref = context.getSharedPreferences(PREFER_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(String key) {
        return pref.getString(key, "");
    }

    public static String getString(String key, String def) {
        return pref.getString(key, def);
    }

    public static void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean def) {
        return pref.getBoolean(key, def);
    }

    public static void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(String key, int def) {
        return pref.getInt(key, def);
    }

    public static void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(String key, long def) {
        return pref.getLong(key, def);
    }

    public static void deleteAll() {
        editor.clear();
        editor.commit();
    }



    public static boolean isAtLeastVersion(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    public static void resetActivityTitle(Activity a) {
        try {
            ActivityInfo info = a.getPackageManager().getActivityInfo(a.getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                a.setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


}