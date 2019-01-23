package com.erobbing.adb_config_demo.sdk.local_config_base;


import com.erobbing.adb_config_demo.sdk.utils.ByteBufferLE;
import com.erobbing.adb_config_demo.sdk.utils.ByteUtils;
import com.erobbing.adb_config_demo.sdk.utils.Crc32;
import com.erobbing.adb_config_demo.sdk.utils.MyLogger;

import java.nio.ByteBuffer;

/*--------------------------------------------------------------------------------------------
 55 AA             4(Bytes)           4(Bytes)                n(Bytes)            4(Bytes)
两字节头         长度字节（=4+n）       命令字                数据内容（Message）      CRC32
--------------------------------------------------------------------------------------------*/
public class PackMsg {
    private MyLogger mLog = MyLogger.jLog();

    public static final long LONG_MAX = 0xffffffffL;

    public PackMsg() {
    }

    public PackMsg(long cmd) {
        this.cmd = cmd;
    }

    public byte[] head = new byte[]{0x55, (byte) 0xaa};
    public long len = 0;
    public long sn = 0;
    public long cmd = 0;
    public byte[] data = null;
    public long crc32 = 0;

    public void fromBuffer(byte[] buff) {
        ByteBuffer bb = ByteBufferLE.wrap(buff);
        bb.get(head);
        len = bb.getInt() & LONG_MAX;
        //bb.get(tag);
        //ver = bb.getInt() & LONG_MAX;
        //sn = bb.getInt() & LONG_MAX;
        cmd = bb.getInt() & LONG_MAX;
        long n = len - 4; //data长度
        if (n > 0) {
            data = new byte[(int) n];
            bb.get(data);
        } else {
            data = null;
        }
        crc32 = bb.getInt() & LONG_MAX;
    }

    public byte[] toBuffer() {
        int nDataLen = data == null ? 0 : data.length;
        int totalLen = 14 + nDataLen;
        len = nDataLen + 4; //data长度 + crc32(long)长度

        ByteBuffer bb = ByteBufferLE.allocate(totalLen);
        bb.put(head);
        bb.putInt((int) (len));
        bb.putInt((int) cmd);
        if (nDataLen > 0) {
            bb.put(data);
        }

        long crc = Crc32.calc(bb.array(), 0, totalLen - 4);
        mLog.v(String.format("crc calc value: %08x", crc));
        bb.put(ByteUtils.uintToBytes(crc));
        return bb.array();
    }
}