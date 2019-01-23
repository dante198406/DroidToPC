package com.erobbing.adb_config_demo.sdk.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by qhb on 17-6-13.
 */

public class SysUtil {
    private static final String TAG = "SysUtil";


    public static void sleepWhile(int milSec) {
        try {
            Thread.sleep(milSec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String PHOTO_CONTRAST_APK_NAME = "ZHYir.apk";

    public static String PHOTO_CONTRAST_APK_PACKAGE = "com.zhy.zhyir";

    public static boolean isPhotoContrastApkInstalled(Context context) {
        boolean installed = false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    PHOTO_CONTRAST_APK_PACKAGE, PackageManager.GET_UNINSTALLED_PACKAGES);
            installed = (info != null);
        } catch (PackageManager.NameNotFoundException e) {
        }
        Log.d(TAG, PHOTO_CONTRAST_APK_PACKAGE + " installed ? " + installed);
        return installed;
    }

    /**
     * ��ȡָ�������İ汾��
     *
     * @param context     ��Ӧ�ó���������
     * @param packageName ����֪���汾��Ϣ��Ӧ�ó���İ���
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(packageName, 0);
            String version = packInfo != null ? packInfo.versionName : "";
            return version;
        } catch (Exception e) {
            MyLogger.jLog().e("get " + packageName + " version name failed!");
            e.printStackTrace();
            return "";
        }
    }

    /**
     * ��ȡָ�������İ汾��
     *
     * @param context     ��Ӧ�ó���������
     * @param packageName ����֪���汾��Ϣ��Ӧ�ó���İ���
     * @return
     * @throws Exception
     */
    public static int getVersionCode(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(packageName, 0);
            int code = packInfo != null ? packInfo.versionCode : -1;
            MyLogger.jLog().d("get " + packageName + " version code: " + code);
            return code;
        } catch (Exception e) {
            MyLogger.jLog().d("get " + packageName + " version code failed!");
            e.printStackTrace();
            return -1;
        }
    }
}
