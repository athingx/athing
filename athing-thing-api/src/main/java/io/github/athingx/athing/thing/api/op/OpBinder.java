package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 绑定器
 *
 * @param <T> 绑定操作类型
 */
public interface OpBinder<T extends OpBind> {

    /**
     * 完成绑定
     *
     * @param bindable 可绑定
     * @return 操作绑定
     */
    CompletableFuture<T> bind(OpBindable bindable);

}
