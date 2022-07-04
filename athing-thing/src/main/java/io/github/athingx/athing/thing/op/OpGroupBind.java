package io.github.athingx.athing.thing.op;

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

    /**
     * 提交操作组绑定
     *
     * @return 操作绑定
     */
    CompletableFuture<OpBinder> commit();

}
