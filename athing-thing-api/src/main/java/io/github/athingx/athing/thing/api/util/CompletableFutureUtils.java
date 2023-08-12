package io.github.athingx.athing.thing.api.util;

import io.github.athingx.athing.thing.api.op.OpReply;
import io.github.athingx.athing.thing.api.op.OpReplyException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * {@link CompletableFuture}函数集合
 */
public class CompletableFutureUtils {

    /**
     * 用于处理结果为{@link OpReply}的成功
     * <ul>
     *     <li>如果应答结果成功，则返回结果；</li>
     *     <li>如果应答结果失败，则抛出{@link OpReplyException}异常</li>
     * </ul>
     *
     * @param <T> 应答结果类型
     * @return function for {@link CompletableFuture#thenCompose(Function)}
     */
    public static <T> Function<OpReply<T>, CompletionStage<T>> thenComposeOpReply() {
        return reply -> reply.isSuccess()
                ? CompletableFuture.completedFuture(reply.data())
                : CompletableFuture.failedFuture(new OpReplyException(reply.token(), reply.code(), reply.desc()));
    }

}
