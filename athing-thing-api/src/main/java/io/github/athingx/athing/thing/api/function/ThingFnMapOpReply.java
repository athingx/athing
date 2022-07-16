package io.github.athingx.athing.thing.api.function;

import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.thing.api.op.OpReply;

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
     * @param type 返回类型
     * @param <V>  应带数据类型
     * @return 映射函数
     */
    static <V> ThingFnMapOpReply<String, OpReply<V>> mappingOpReplyFromJson(TypeToken<OpReply<V>> type) {
        return (topic, json) -> ThingFnMapJson.<OpReply<V>>mappingJsonToType(type.getType()).mapping(topic, json);
    }

}
