package io.github.athingx.athing.common.util;

import java.util.Objects;

public class StringUtils {

    /**
     * 字节数组转16进制字符串
     *
     * @param bArray 目标字节数组
     * @return 16进制字符串
     */
    public static String bytesToHexString(final byte[] bArray) {
        final StringBuilder sb = new StringBuilder(bArray.length * 2);
        for (byte b : bArray) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static boolean equalsIgnoreCase(String expect, String actual) {
        return Objects.equals(expect, actual) || (Objects.nonNull(expect) && expect.equalsIgnoreCase(actual));
    }

    /**
     * 判断目标字符串是否在指定字符串集合中，并忽略大小写
     *
     * @param target  目标字符串
     * @param strings 指定字符串集合
     * @return TRUE | FALSE
     */
    public static boolean isInIgnoreCase(String target, String... strings) {
        if (null != strings) {
            for (final var string : strings) {
                if (equalsIgnoreCase(target, string)) {
                    return true;
                }
            }
        }
        return false;
    }

}
