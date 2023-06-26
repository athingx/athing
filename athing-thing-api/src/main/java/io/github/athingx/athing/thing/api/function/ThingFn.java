package io.github.athingx.athing.thing.api.function;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.domain.OpReply;

import java.nio.charset.Charset;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

/**
 * 设备函数集合
 */
public interface ThingFn {

    /**
     * 匹配：消息主题
     *
     * @param fn  主题匹配函数
     * @param <T> 消息类型
     * @return 匹配函数
     */
    static <T> BiPredicate<String, T> matchingTopic(Predicate<String> fn) {
        return (topic, t) -> fn.test(topic);
    }

    /**
     * 映射：{@code byte[]->json}
     *
     * @param charset 字符串编码
     * @return 映射函数
     */
    static Function<byte[], String> mappingByteToJson(Charset charset) {
        return bytes -> new String(bytes, charset);
    }

    /**
     * 映射：{@code json->}{@link OpReply}
     *
     * @param type 应答数据类型
     * @param <T>  应答数据类型
     * @return 映射函数
     */
    static <T> Function<String, T> mappingJsonToType(Class<T> type) {
        return json -> GsonFactory.getGson().fromJson(json, type);
    }

    /**
     * 映射：{@code json->T<V>}
     *
     * @param tToken {@link TypeToken}为Gson解决{@code T<V>}序列化问题
     * @param <T>    应答数据类型
     * @return 映射函数
     */
    static <T> Function<String, T> mappingJsonToType(TypeToken<T> tToken) {
        return json -> GsonFactory.getGson().fromJson(json, tToken.getType());
    }

    /**
     * 映射：{@code json->}{@link OpReply}
     *
     * @param type 应答数据类型
     * @param <T>  应答数据类型
     * @return 映射函数
     */
    static <T> Function<String, OpReply<T>> mappingJsonToOpReply(Class<T> type) {
        return json -> {
            final JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            return OpReply.reply(
                    ofNullable(root.get("id"))
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    ofNullable(root.get("code"))
                            .map(JsonElement::getAsInt)
                            .orElse(0),
                    ofNullable(root.get("message"))
                            .map(JsonElement::getAsString)
                            .orElse(null),
                    ofNullable(root.get("data"))
                            .filter(element -> !element.getAsJsonObject().keySet().isEmpty())
                            .map(element -> GsonFactory.getGson().fromJson(element, type))
                            .orElse(null)
            );
        };
    }

}
