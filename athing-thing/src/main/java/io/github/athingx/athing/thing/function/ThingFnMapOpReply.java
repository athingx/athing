package io.github.athingx.athing.thing.function;

import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.thing.ThingPath;
import io.github.athingx.athing.thing.op.OpReply;
import io.github.athingx.athing.thing.op.ThingOpException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;

/**
 * 操作应答映射函数
 *
 * @param <T> 原始类型
 * @param <R> 目标应答数据类型
 */
@FunctionalInterface
public interface ThingFnMapOpReply<T, R> extends ThingFnMap<T, R> {

    /**
     * 从Json字符串映射到操作应答
     *
     * @param supplier 映射解析函数
     * @param <V>      应带数据类型
     * @return 映射函数
     */
    static <V> ThingFnMapOpReply<String, OpReply<V>> mappingOpReplyFromJson(Supplier<TypeToken<OpReply<V>>> supplier) {
        return (topic, json) -> GsonFactory.getGson().fromJson(json, supplier.get().getType());
    }

    /**
     * 从Json字符串映射到操作应答
     *
     * @param <V> 应答数据类型
     * @return 映射函数
     */
    static <V> ThingFnMapOpReply<String, OpReply<V>> mappingOpReplyFromJson() {
        return mappingOpReplyFromJson(() -> new TypeToken<>() {
        });
    }

    /**
     * @see #mappingOpReplyFromJson()
     */
    static <V> ThingFnMapOpReply<String, OpReply<V>> mappingOpReplyFromJson(Class<V> type) {
        return mappingOpReplyFromJson(() -> new TypeToken<>() {
        });
    }

    /**
     * 从{@link OpReply}映射其中的数据
     *
     * @param path 设备路径（应答出错时抛出异常{@link ThingOpException}）
     * @param <V>  应答数据类型
     * @return 映射函数
     */
    static <V> ThingFnMapOpReply<OpReply<V>, CompletableFuture<V>> mappingOpReplyToData(ThingPath path) {
        return (topic, reply) -> reply.isOk()
                ? completedFuture(reply.data())
                : failedFuture(new ThingOpException(path, reply.code(), reply.desc()));
    }

}
