package com.york.media.opengl.utils;

/**
 * author : York
 * date   : 2020/12/21 1:32
 * desc   : log 工具
 */
public class LogUtil {

    private static String TAG = "Log_York";

    public static void i(String msg) {
        android.util.Log.i(TAG, msg);
    }

    public static void d(String msg) {
        android.util.Log.d(TAG, msg);
    }

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }

}
