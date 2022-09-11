package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 操作批量绑定
 */
public interface OpBatchBinding extends OpBindable {

    /**
     * 批量提交绑定
     *
     * @return 操作绑定
     */
    CompletableFuture<OpBind> commit();

}
