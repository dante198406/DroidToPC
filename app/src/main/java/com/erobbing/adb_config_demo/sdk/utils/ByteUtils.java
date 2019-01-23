package com.erobbing.adb_config_demo.sdk.utils;


import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteUtils {
    /**
     * UINT 转为 4字节数组
     */
    public static byte[] uintToBytes(long x) {
        ByteBuffer buffer = ByteBufferLE.allocate(4);
        buffer.putInt((int) x);
        return buffer.array();
    }

    /**
     * 4字节数组转为 UINT
     */
    public static long bytesToUInt(byte[] bytes) {
        MyAssert.Assert(4, bytes.length, "BytesToUInt");
        ByteBuffer buffer = ByteBufferLE.wrap(bytes);
        return buffer.getInt() & 0xffffffffL;

    }

    /**
     * 4字节数组转为 UINT，大端
     */
    public static long bytesToUIntBig(byte[] bytes) {
        MyAssert.Assert(4, bytes.length, "BytesToUInt");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt() & 0xffffffffL;

    }

    /**
     * 从指定位置，转换两个字节为 int
     *
     * @param buff
     * @param offset
     * @return
     */
    public static int makeUShort(byte[] buff, int offset) {
        ByteBuffer bb = ByteBufferLE.wrap(ArrayUtils.subarray(buff, offset, offset + 2));
        return bb.getShort() & 0xffff;
    }

    public static int makeUShort(byte[] buff) {
        ByteBuffer bb = ByteBufferLE.wrap(buff);
        return bb.getShort() & 0xffff;
    }

    public static int makeUShortBig(byte[] buff) {
        ByteBuffer bb = ByteBuffer.wrap(buff);
        bb.order(ByteOrder.BIG_ENDIAN);
        return bb.getShort() & 0xffff;
    }


    public static byte[] ushortToBytes(int value) {
        ByteBuffer bb = ByteBufferLE.allocate(2);
        bb.putShort((short) value);
        return bb.array();
    }

}