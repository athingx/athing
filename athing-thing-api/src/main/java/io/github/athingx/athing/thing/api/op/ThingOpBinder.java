package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备操作绑定
 */
public interface ThingOpBinder {

    /**
     * 取消绑定
     *
     * @return 取消结果
     */
    CompletableFuture<Void> unbind();

}
