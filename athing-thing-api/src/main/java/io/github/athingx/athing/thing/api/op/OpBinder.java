package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 操作绑定器
 */
public interface OpBinder {

    /**
     * 取消绑定
     *
     * @return 取消结果
     */
    CompletableFuture<Void> unbind();

}
