package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 操作组绑定
 */
public interface OpGroupBinding {

    /**
     * 绑定
     *
     * @see ThingOp#binding(String)
     */
    OpBinding<byte[]> binding(String express);

    default <T extends OpBind> CompletableFuture<T> bindFor(OpGroupBinder<T> binder) {
        return binder.bindFor(this);
    }

    /**
     * 提交操作组绑定
     *
     * @return 操作绑定
     */
    CompletableFuture<OpBind> commit();

}
