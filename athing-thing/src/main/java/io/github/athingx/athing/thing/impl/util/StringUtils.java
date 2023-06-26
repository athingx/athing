package io.github.athingx.athing.thing.impl.util;

public class StringUtils {

    /**
     * 字节数组转16进制字符串
     *
     * @param bArray 目标字节数组
     * @return 16进制字符串
     */
    public static String bytesToHexString(final byte[] bArray) {
        final StringBuilder sb = new StringBuilder(bArray.length * 2);
        for (byte b : bArray)
            sb.append(String.format("%02X", b));
        return sb.toString();
    }

}
