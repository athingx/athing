package io.github.athingx.athing.platform.impl.util;

import java.util.Objects;

public class IOUtils {

    public static void closeQuietly(AutoCloseable closeable) {

        if (Objects.isNull(closeable)) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception cause) {
            // ignore...
        }

    }

}
