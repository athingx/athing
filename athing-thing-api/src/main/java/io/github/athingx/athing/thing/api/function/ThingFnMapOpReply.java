package io.github.athingx.athing.thing.api.function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.thing.api.op.OpReply;

import static java.util.Optional.ofNullable;

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
     * @param type 应答数据类型
     * @param <V>  应答数据类型
     * @return 映射函数
     */
    static <V> ThingFnMapOpReply<String, OpReply<V>> mappingOpReplyFromJson(TypeToken<OpReply<V>> type) {
        return (topic, json) -> ThingFnMapJson.<OpReply<V>>mappingJsonToType(type.getType()).mapping(topic, json);
    }

    /**
     * 从Json字符串映射到操作应答
     *
     * @param type 应答数据类型
     * @param <V>  应答数据类型
     * @return 映射函数
     */
    static <V> ThingFnMapOpReply<String, OpReply<V>> mappingOpReplyFromJson(Class<V> type) {
        return (topic, json) -> {
            final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return OpReply.reply(
                    ofNullable(root.get("id")).map(JsonElement::getAsString).orElse(null),
                    ofNullable(root.get("code")).map(JsonElement::getAsInt).orElse(0),
                    ofNullable(root.get("message")).map(JsonElement::getAsString).orElse(null),
                    ofNullable(root.get("data")).map(element -> GsonFactory.getGson().fromJson(element, type)).orElse(null)
            );
        };
    }

}
