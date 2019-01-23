package com.erobbing.adb_config_demo.sdk.utils;

import java.util.zip.CRC32;

/**
 * Created by quhuabo on 2017/7/3 0003.
 */

public class Crc32 {

    public static long calc(byte buff[], int startIndex, int endIndex) {
        CRC32 c = new CRC32();
        c.update(buff, startIndex, endIndex - startIndex);
        return c.getValue();
    }

    public static long calc(byte buff[]) {
        CRC32 c = new CRC32();
        c.update(buff);
        return c.getValue();
    }

    public static boolean equalCrc(byte buff[], long targetCrc) {
        return calc(buff) == targetCrc;
    }

    public static boolean equalCrc(byte buff[], int startIndex, int endIndex, long targetCrc) {
        return calc(buff, startIndex, endIndex) == targetCrc;
    }

}
