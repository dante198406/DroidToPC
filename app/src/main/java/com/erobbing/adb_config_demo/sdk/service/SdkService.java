package com.erobbing.adb_config_demo.sdk.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;

import com.erobbing.adb_config_demo.sdk.local_config.MyLocalConfig;
import com.erobbing.adb_config_demo.sdk.local_config_base.LocalConfigServer;
import com.erobbing.adb_config_demo.R;

public class SdkService extends Service {

    private LocalConfigServer localConfigServer;

    public SdkService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        /**
         *  创建通知栏管理工具
         */

        NotificationManager notificationManager = (NotificationManager) getSystemService
                (NOTIFICATION_SERVICE);

        /**
         *  实例化通知栏构造器
         */

        Notification.Builder mBuilder = new Notification.Builder(this);

        /**
         *  设置Builder
         */
        //设置标题
        mBuilder.setContentTitle("钥匙箱配置服务")
                //设置内容
                .setContentText("服务正在运行")
                //设置大图标
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher_round)
                //设置通知时间
                .setWhen(System.currentTimeMillis())
        //首次进入时显示效果
        //设置通知方式，声音，震动，呼吸灯等效果，这里通知方式为声音
        //.setDefaults(Notification.DEFAULT_SOUND)
        //.setAutoCancel(true).setOngoing(true);
        ;

        //发送通知请求
        Notification notify = mBuilder.build();
        //notificationManager.notify(10, notify);
        startForeground(10, notify);

    }

    private void init() {
        localConfigServer = new MyLocalConfig(this);
        localConfigServer.setContext(this);
        localConfigServer.start();
    }

    @Override
    public void onDestroy() {
        localConfigServer.stop();
        stopForeground(true);
        super.onDestroy();
    }

}
