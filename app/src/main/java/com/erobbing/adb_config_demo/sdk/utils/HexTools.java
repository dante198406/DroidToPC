package com.erobbing.adb_config_demo.sdk.utils;

/**
 * Created by quhuabo on 2016/9/7 0007.
 */

public class HexTools {

    /**
     * 字节数组转16进制字符串，当参数为 null 时返回 "<null>"
     *
     * @param a
     * @return
     */
    public static String byteArrayToHex(byte[] a) {
        if (null == a) {
            return "<null>";
        }
        StringBuilder sb = new StringBuilder(a.length * 3 - 1);
        int i = 0;
        for (byte b : a) {
            if (i != 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", b & 0xff));
            ++i;
        }

        return sb.toString();
    }

    /**
     * 多余的字节会有省略号显示
     */
    public static String byteArrayToHexReadable(byte[] a, int count) {
        if (a == null) {
            return "null hex data";
        }

        String temp = "总字节数: " + a.length + ": " + HexTools.byteArrayToHex(a, 0, count);
        if (a.length > count) {
            temp += " ... (hidden bytes: " + (a.length - count) + ")";
        } else {
            temp += "(no hidden bytes)";
        }
        return temp;
    }

    /**
     * 多余的字节会有省略号显示
     */
    public static String byteArrayToHexReadable(byte[] a) {
        return byteArrayToHexReadable(a, a.length);
    }

    public static String byteArrayToHex(byte[] a, int offset, int count) {
        if (null == a) {
            return "<null>";
        }

        StringBuilder sb = new StringBuilder(count * 3 - 1);
        for (int i = offset; i < offset + count && i < a.length; ++i) {
            if (i != offset) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", a[i] & 0xff));
        }
        return sb.toString();
    }

    /**
     * 16进制字符串转字节数组，可以空格分开，也可以没有空格
     *
     * @param hexStr
     * @return
     */
    public static byte[] HexToByteArray(String hexStr) {
        if (hexStr == null) return null;
        hexStr = hexStr.trim();
        if (hexStr.length() == 0) return null;

        String[] temp;
        if (hexStr.contains(" ")) {
            temp = hexStr.split(" ");

        } else {
            int n = hexStr.length() / 2;
            if (hexStr.length() % 2 != 0) {
                hexStr += " "; // 凑够偶数
                n++;
            }

            temp = new String[n];
            for (int i = 0; i < n; ++i) {
                temp[i] = hexStr.substring(i * 2, i * 2 + 2).trim();
            }
        }

        byte[] bytesArray = new byte[temp.length];
        int index = 0;
        for (String item : temp) {
            bytesArray[index] = (byte) (Integer.parseInt(item, 16) & 0xff);
            index++;
        }
        return bytesArray;
    }
}
