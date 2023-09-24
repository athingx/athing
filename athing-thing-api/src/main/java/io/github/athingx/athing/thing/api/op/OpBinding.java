package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 绑定设备操作
 *
 * @param <T>
 */
public interface OpBinding<T extends OpBinder> {

    /**
     * 绑定设备操作
     *
     * @param op 设备操作
     * @return 设备操作
     */
    CompletableFuture<T> binding(ThingOp<byte[], byte[]> op);

}
