package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 操作组绑定器
 *
 * @param <T> 操作绑定类型
 */
public interface OpGroupBindFor<T extends OpBind> {

    /**
     * 绑定
     *
     * @param group 操作组绑定
     * @return 操作绑定
     */
    CompletableFuture<T> bindFor(OpGroupBinding group);

}
