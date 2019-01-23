package com.erobbing.adb_config_demo.sdk.local_config.dealers;

import com.erobbing.adb_config_demo.AdbConfigDemo;
import com.erobbing.adb_config_demo.sdk.local_config_base.LocalConfigServer;
import com.erobbing.adb_config_demo.sdk.local_config_base.PackMsg;
import com.erobbing.adb_config_demo.sdk.utils.MyLogger;


public abstract class DealerBase implements Dealer {
    protected static MyLogger mLog = MyLogger.jLog();

    public static void send_resp(long cmd, AdbConfigDemo.Config msg) {
        PackMsg m = new PackMsg();
        m.cmd = cmd;
        m.data = msg.toByteArray();
        LocalConfigServer.pushToSendList(m, 0, "");
    }
}
