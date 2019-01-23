package com.erobbing.adb_config_demo.sdk.local_config;

import android.content.Context;
import android.content.SharedPreferences;

import com.erobbing.adb_config_demo.AdbConfigDemo;
import com.erobbing.adb_config_demo.sdk.local_config.dealers.Dealer;
import com.erobbing.adb_config_demo.sdk.local_config.dealers.DealerBase;
import com.erobbing.adb_config_demo.sdk.local_config_base.LocalConfigServer;
import com.erobbing.adb_config_demo.sdk.local_config_base.PackMsg;
import com.erobbing.adb_config_demo.sdk.service.SdkService;
import com.erobbing.adb_config_demo.sdk.utils.MyLogger;

import java.util.HashMap;
import java.util.Map;

public class MyLocalConfig extends LocalConfigServer {
    private MyLogger mLog = MyLogger.jLog();
    private SdkService mService;

    public MyLocalConfig(SdkService sdkService) {
        // 修改端口号
        mListenPort = 10087;
        mService = sdkService;
    }


    /**
     * 不同命令的执行表
     */
    private Map<Integer, Dealer> cmdMap = new HashMap<Integer, Dealer>() {
        {

            put(AdbConfigDemo.Cmd.cmdReadConfig_VALUE, new DealerBase() {
                @Override
                public void doCmd(long cmd, byte[] data) throws Exception {
                    mLog.i("获取到读配置指令");

                    SharedPreferences sp = mService.getSharedPreferences("config", Context.MODE_PRIVATE);

                    AdbConfigDemo.Config.Builder builder = AdbConfigDemo.Config.newBuilder();
                    builder.setErrorCode(AdbConfigDemo.ErrorCode.OK_VALUE);

                    builder.setBoxID(sp.getString("box_id", ""));
                    builder.setShopID(sp.getString("shop_id", ""));
                    builder.setKeyID(sp.getString("key_id", ""));

                    // TODO: 设置其他值

                    // 发送消息给客户端
                    send_resp(cmd, builder.build());
                }
            });

            put(AdbConfigDemo.Cmd.cmdWriteConfig_VALUE, new DealerBase() {
                @Override
                public void doCmd(long cmd, byte[] data) throws Exception {
                    mLog.i("获取到写配置指令");

                    // 解析读到的数据
                    AdbConfigDemo.Config config = AdbConfigDemo.Config.parseFrom(data);

                    SharedPreferences sp = mService.getSharedPreferences("config", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putString("shop_id", config.getShopID());
                    ed.putString("box_id", config.getBoxID());
                    ed.putString("key_id", config.getKeyID());
                    ed.commit();

                    // TODO: 保存其他值

                    // 返回消息
                    AdbConfigDemo.Config.Builder builder = AdbConfigDemo.Config.newBuilder();
                    builder.setErrorCode(AdbConfigDemo.ErrorCode.OK_VALUE);
                    send_resp(cmd, builder.build());
                }
            });


            // TODO: 在这里增加其他的命令处理方法
        }
    };


    @Override
    public void doWithPack(PackMsg msg) throws Exception {
        Dealer dealer = cmdMap.get((int) msg.cmd);
        if (dealer != null) {
            dealer.doCmd(msg.cmd, msg.data);
        } else {
            mLog.w("不认识的命令字：" + msg.cmd);

            // 原样返回
            AdbConfigDemo.Config.Builder resp = AdbConfigDemo.Config.newBuilder();
            resp.setErrorCode(AdbConfigDemo.ErrorCode.UNKNOWN_CMD_VALUE);
            DealerBase.send_resp(msg.cmd, resp.build());
        }
    }

}
