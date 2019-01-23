package com.erobbing.adb_config_demo.sdk.local_config_base;

import android.os.Handler;
import android.os.Looper;

import com.erobbing.adb_config_demo.sdk.model.Result;
import com.erobbing.adb_config_demo.sdk.model.ResultStage;
import com.erobbing.adb_config_demo.sdk.parse.Item;
import com.erobbing.adb_config_demo.sdk.parse.ParseException;
import com.erobbing.adb_config_demo.sdk.parse.ParserListener;
import com.erobbing.adb_config_demo.sdk.utils.ByteUtils;
import com.erobbing.adb_config_demo.sdk.utils.Crc32;
import com.erobbing.adb_config_demo.sdk.utils.HexTools;
import com.erobbing.adb_config_demo.sdk.utils.MyAssert;
import com.erobbing.adb_config_demo.sdk.utils.MyLogger;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by quhuabo on 2017/7/3
 * 从 DiagramParserZ1 复制，用于与PC端通讯的Socket解析处理
 */

public class LocalConfigParser {

    /*public static int ITEM_HEAD = 0;
    public static int ITEM_LEN = 1;
    public static int TAG = 2;
    public static int VER = 3;
    public static int sn = 4;
    public static int cmd = 5;
    public static int ITEM_DATA = 6;
    public static int ITEM_CRC = 7;*/

    public static int ITEM_HEAD = 0;
    public static int ITEM_LEN = 1;
    public static int cmd = 2;
    public static int ITEM_DATA = 3;
    public static int ITEM_CRC = 4;

    private MyLogger mLog = MyLogger.jLog();

    ArrayList<Item> mItems = new ArrayList<Item>();

    Result mResult = null;
    Item mData = new Item(0);   // 变长 的数据
    private byte[] mBuffer = null;
    int mStep = 0;
    private byte[] mPackData;
    private ParserListener mListener;       // 异步 listener(post 到主线程来执行)
    private ParserListener mSyncListener;   // 同步 listener
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public LocalConfigParser() {
        initial();
    }

    public byte[] getRemain() {
        return mBuffer;
    }

    private void initial() {
        mItems.add(new Item(new byte[]{0x55, (byte) 0xaa}));  //0
        mItems.add(new Item(4)); //1. len
        mItems.add(new Item(4)); //5. cmd
        mItems.add(new Item(0)); //6. data
        mItems.add(new Item(4)); //7. crc32
    }

    public int getByteAt(int itemIndex) {
        int ret = 0;
        for (int i = 0; i < itemIndex; ++i) {
            ret += mItems.get(i).getNeedLen();
        }
        return ret;
    }

    public int getDataLen(int itemIndex) {
        return mItems.get(itemIndex).get_recogized_len();
    }

    private boolean isStageOK(List<Item> items) {
        for (Item i : items) {
            if (i.getStage() != ResultStage.Ok) {
                return false;
            }
        }
        return true;
    }

    public int getStep() {
        return mStep;
    }

    public int getStepCount() {
        return mItems.size();
    }


    /**
     * 组合报文解析
     * 会接收多个或不足一个包的数据
     *
     * @param ABuffer
     * @throws ParseException
     */
    public void parse(byte[] ABuffer) throws ParseException {
        if (ABuffer == null) {
            throw new ParseException("参数为 null ???");
        }
        MyAssert.Assert(true, ABuffer != this.mBuffer, "DiagramParserZ1.parse");

        this.mBuffer = ArrayUtils.addAll(this.mBuffer, ABuffer);

        LOOP_PACKAGE:
        /* 解析每一包 */
        do {
            try {
                // 解析每包中的每一个部分
                int j = mStep;
                for (; j < mItems.size(); ++j, ++mStep) {
                    Item i = mItems.get(j);

                    if (mBuffer == null) {
                        mLog.v("no data to parsed, so break LOOP_PACKAGE, current step: " + j + ", got count: " + i.get_recogized_len() + "/" + i.getNeedLen());
                        break LOOP_PACKAGE;
                    }


                    mBuffer = i.parse(mBuffer);
                    if (i.isParseOk()) {
                        mLog.v("got item: (" + i.getNeedLen() + ")" + HexTools.byteArrayToHex(i.get_recognized()));
                        // 解析成功，继续下一个解析
                        if (j == ITEM_LEN) {
                            long dataLen = ByteUtils.bytesToUInt(i.get_recognized());
                            mLog.v("data block len: " + dataLen);
                            if (dataLen < 4) {//data长度 + crc32(long)长度
                                throw new ParseException("parse error, data len must be >= " + 4);
                            }

                            mItems.get(ITEM_DATA).setNeedLen((int) (dataLen) - 4);
                        }


                    } else if (i.getStage() == ResultStage.Fail) {
                        throw new ParseException("parse error, step: " + mStep);

                    } else {
                        // cached 状态下直接返回
                        mLog.v("no data to parsed, so break LOOP_PACKAGE");
                        break LOOP_PACKAGE;
                    }

                }

                mPackData = makePackData();

                if (!checkCrc()) {
                    throw new ParseException("parse error, crc error.");
                }

                final byte[] byteTemp = mPackData;
                mLog.v("got [ Good ] package: " + HexTools.byteArrayToHexReadable(mPackData, 50));
                if (getListener() != null) {

                    // 发送到主线程去执行解析接收包的任务
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getListener().onGotPackage(byteTemp);
                        }
                    });
                }

                if (mSyncListener != null) {
                    mSyncListener.onGotPackage(byteTemp);
                }

            } catch (ParseException e) {
                e.printStackTrace();
                mLog.e("ParseException occurred: " + e.getMessage() + ", will reset all cached buffer.");
                mBuffer = null;
            }

            // 重置分段解析状态，并解析下一包。
            resetParseStatus();


        } while (mBuffer != null);

        mLog.v("parse all finished, current step: " + mStep + ", max step: " + mItems.size());
    }

    /**
     * 重置解析状态，但Buffer不能清空，此函数不可为 public，仅仅由解析函数调用
     * 仅当解析完一包，或解析出错时调用
     */
    private void resetParseStatus() {
        mStep = 0;
        for (Item i : mItems) {
            i.reset();
        }
    }

    /**
     * 清除所有信息，包括缓存的数据
     * 用于初始化时调用。
     */
    public void resetAll() {
        resetParseStatus();
        mBuffer = null;
    }


    /**
     * 生成字节数据包
     *
     * @return
     */
    private byte[] makePackData() {

        if (mStep != mItems.size()) {
            mLog.w("pack not parsed.");
            return null;
        }

        byte[] result = null;
        int nLen = 0;
        for (int i = 0; i < mItems.size(); ++i) {
            nLen += mItems.get(i).get_recogized_len();
        }

        result = new byte[nLen];
        int k = 0;
        for (int i = 0; i < mItems.size(); ++i) {
            int copyLen = mItems.get(i).get_recogized_len();
            if (copyLen > 0) {
                System.arraycopy(mItems.get(i).get_recognized(), 0, result, k, copyLen);
                k += copyLen;
            }
        }
        return result;
    }

    private static byte[] TEST_CRC = new byte[]{(byte) 0xfe, (byte) 0xfe, (byte) 0xfe, (byte) 0xfe};

    /**
     * 检验检验字是否正确
     */
    private boolean checkCrc() throws ParseException {

        // 后门，用于测试
        if (Arrays.equals(Arrays.copyOfRange(mPackData, mPackData.length - 4, mPackData.length), TEST_CRC)) {
            return true;
        }

        long crc1 = ByteUtils.bytesToUInt(mItems.get(ITEM_CRC).get_recognized());

        return Crc32.equalCrc(mPackData, 0, mPackData.length - 4, crc1);

    }

    public byte[] getPackData() {
        return mPackData;
    }

    public ParserListener getListener() {
        return mListener;
    }

    /**
     * 设置异步回调，post 到主线程中去执行
     */
    public void setListener(ParserListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置同步回调，相同线程
     */
    public void setSyncListener(ParserListener syncListener) {
        this.mSyncListener = syncListener;
    }


}

