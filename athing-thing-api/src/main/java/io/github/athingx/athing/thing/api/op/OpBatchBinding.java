package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 操作批量绑定
 */
public interface OpBatchBinding {

    /**
     * 绑定，必须等待{@link #commit()}提交后，绑定才能算完成
     * @see ThingOp#binding(String)
     *
     */
    OpBinding<byte[]> binding(String express);

    /**
     * 批量提交绑定
     *
     * @return 操作绑定
     */
    CompletableFuture<OpBind> commit();

}
