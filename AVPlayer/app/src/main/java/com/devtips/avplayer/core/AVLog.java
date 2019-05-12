package com.devtips.avplayer.core;

import android.util.Log;

public class AVLog {

    private static final String TAG = "AVKit";

    private static boolean DEBUG = true;

    public static void setDebug(boolean debug) {
        AVLog.DEBUG = debug;
    }

    public static void e(String msg,Object... formats) {
        if (DEBUG)
            Log.e(TAG,String.format(msg,formats));

        printStackTrack();
    }

    public static void i(String msg,Object... formats) {
        if (DEBUG)
            Log.i(TAG,String.format(msg,formats));
    }

    public static void d(String msg,Object... formats) {
        if (DEBUG)
            Log.d(TAG,String.format(msg,formats));
    }

    public static void v(String msg,Object... formats) {
        if (DEBUG) {
            Log.v(TAG, String.format(msg, formats));
        }
    }

    /**
     * 遍历StackTrace中的内容并遍历StackTraceElement数组
     * 请注意观察此处的输出信息.
     */
    private static void printStackTrack(){
        java.util.Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();

        StackTraceElement[] ste = ts.get(Thread.currentThread());

        for (StackTraceElement s : ste) {

            Log.e(TAG, s.toString());

        }


    }
}
