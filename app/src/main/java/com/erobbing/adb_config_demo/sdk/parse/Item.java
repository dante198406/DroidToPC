package com.erobbing.adb_config_demo.sdk.parse;


/**
 * author: quhuabo, created on 2016/8/25 0029.
 * 解析组合报文中的某一项
 */

import com.erobbing.adb_config_demo.sdk.model.Result;
import com.erobbing.adb_config_demo.sdk.model.ResultStage;
import com.erobbing.adb_config_demo.sdk.utils.HexTools;
import com.erobbing.adb_config_demo.sdk.utils.MyLogger;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public class Item extends Result implements Parser {
    private MyLogger mLog = MyLogger.jLog();
    private int mNeedLen = 0;
    private byte[] mMatchValue = null;

    /**
     * 需要的长度，需要匹配的值
     *
     * @param matchValue，当为 NULL 时不需要匹配
     */
    public Item(byte[] matchValue) {
        mMatchValue = matchValue;
        setNeedLen(matchValue.length);
    }

    public Item(int len) {
        setNeedLen(len);
    }

    public int getNeedLen() {
        return mNeedLen;
    }

    public void setNeedLen(int mNeedLen) {
        this.mNeedLen = mNeedLen;
    }

    /**
     * 解析 buff
     *
     * @param buff
     * @return 剩余的数组
     */
    @Override
    public byte[] parse(byte[] buff) {
        byte[] ret = null;
        int nWantLen = getNeedLen() - get_recogized_len();
        if (buff.length < nWantLen) {
            addBuffer(buff, buff.length);
            ret = null;
        } else {
            if (nWantLen > 0) {
                addBuffer(buff, nWantLen);//set recognized
                ret = ArrayUtils.subarray(buff, nWantLen, buff.length);
                if (mMatchValue != null) {
                    if (Arrays.equals(get_recognized(), mMatchValue)) {
                        setStage(ResultStage.Ok);
                    } else {
                        mLog.w("parse failed, expected: " + HexTools.byteArrayToHex(mMatchValue) + ", but got: " + HexTools.byteArrayToHex(get_recognized()));
                        setStage(ResultStage.Fail);
                    }
                } else {
                    setStage(ResultStage.Ok);
                }
            } else if (nWantLen == 0) {
                ret = buff;
                setStage(ResultStage.Ok);
            }
        }
        if (ret != null && ret.length == 0) {
            ret = null;
        }
        return ret;
    }

}