package io.github.athingx.athing.common.util;

import java.io.Closeable;
import java.nio.charset.Charset;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

/**
 * I/O操作工具类
 */
public class IOUtils {

    /**
     * 加载文本资源
     *
     * @param path    资源路径
     * @param charset 字符编码
     * @return 文本
     */
    public static String getStringResource(String path, Charset charset) {
        final ClassLoader loader = IOUtils.class.getClassLoader();
        final StringBuilder stringSb = new StringBuilder();
        try (final Scanner scanner = new Scanner(requireNonNull(loader.getResourceAsStream(path)), charset.name())) {
            while (scanner.hasNextLine()) {
                stringSb.append(scanner.nextLine()).append("\n");
            }
        }
        return stringSb.toString();
    }

    /**
     * 关闭可关闭对象
     *
     * @param closeable 可关闭对象
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (Exception cause) {
            // ignore...
        }
    }

}
