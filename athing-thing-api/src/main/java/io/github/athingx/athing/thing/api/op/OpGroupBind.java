package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 操作组绑定
 */
public interface OpGroupBind {

    /**
     * 绑定
     *
     * @see ThingOp#bind(String)
     */
    OpBind<byte[]> bind(String express);

    default <T extends OpBinder> CompletableFuture<T> binding(OpBinding<T> binding) {
        return binding.binding(this);
    }

    /**
     * 提交操作组绑定
     *
     * @return 操作绑定
     */
    CompletableFuture<OpBinder> commit();

}
