package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备绑定
 */
public interface ThingBind {

    /**
     * 接绑
     *
     * @return 接绑结果
     */
    CompletableFuture<Void> unbind();

}
