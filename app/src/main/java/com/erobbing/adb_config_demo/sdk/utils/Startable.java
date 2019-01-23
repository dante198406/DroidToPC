package com.erobbing.adb_config_demo.sdk.utils;

public interface Startable {
    String getName();

    boolean isStarted();

    void start();

    void stop();

    void restart();
}
