package com.erobbing.adb_config_demo.sdk.local_config.dealers;

public interface Dealer {
    void doCmd(long cmd, byte data[]) throws Exception;
}
