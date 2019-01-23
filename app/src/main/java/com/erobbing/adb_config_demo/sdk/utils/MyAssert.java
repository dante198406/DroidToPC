package com.erobbing.adb_config_demo.sdk.utils;

/**
 * Created by Administrator on 2016/11/25 0025.
 */

public class MyAssert {
    public static void Assert(int want, int got, String info) {
        if (want != got) {
            String str = String.format("assert failed[ %s ]: want %d, but got %d", info, want, got);
            throw new RuntimeException(str);
        } else {
            MyLogger.jLog().i("MyAssert: " + info + ", [ok]");
        }
    }

    public static void Assert(boolean want, boolean got, String info) {
        if (want != got) {
            String str = String.format("assert failed[ %s ]: want %s, but got %s", info, String.valueOf(want)
                    , String.valueOf(got));
            throw new RuntimeException(str);
        } else {
            MyLogger.jLog().i("MyAssert: " + info + ", [ok]");
        }
    }


}
