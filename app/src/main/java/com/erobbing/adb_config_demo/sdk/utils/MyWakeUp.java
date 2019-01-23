package com.erobbing.adb_config_demo.sdk.utils;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class MyWakeUp {

    public static void releaseDelay(final PowerManager.WakeLock wl, int milseconds) {
        try {
            if (wl == null) {
                Log.i("MyWakeUp", "releaseDelay: wl is null");
                return;
            }

            if (milseconds <= 0) {
                wl.release();
            } else {
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        try {
                            wl.release();
                        } catch (Exception e) {
                            Log.d("MyWakeUp", "no release: " + e.getMessage());
                        }
                    }
                }, milseconds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final PowerManager.WakeLock wakeLock(Context context) {
        return wakeLock(context, 0);
    }

    /**
     * 自动关闭休眠锁,
     *
     * @param _timeoutMs 毫秒
     */
    public static final PowerManager.WakeLock wakeLock(Context context, long _timeoutMs) {
        try {
            // 获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            // 点亮屏幕
            //            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
            //					| PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_DIM_WAKE_LOCK, "NetCamera");

			/* quhuabo: 2017-02-20 取消点亮屏幕 */
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NetCamera");
            wl.setReferenceCounted(false);

            wl.acquire(_timeoutMs);

            MyLogger.jLog().i("启动保持唤醒（wakeup），timeout_MS: " + _timeoutMs);
            return wl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 自动关闭休眠锁
     */
    public static final PowerManager.WakeLock wakeLockScreen(Context context, long _timeout) {
        try {
            // 获取电源管理器对象
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            // 点亮屏幕
            final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE | PowerManager.SCREEN_DIM_WAKE_LOCK, "NetCamera");

			/* quhuabo: 2017-02-20 取消点亮屏幕 */
            //			final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NetCamera");
            wl.setReferenceCounted(false);

            wl.acquire(_timeout);

            MyLogger.jLog().i("启动保持唤醒（wakeup），timeout: " + _timeout);
            return wl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}