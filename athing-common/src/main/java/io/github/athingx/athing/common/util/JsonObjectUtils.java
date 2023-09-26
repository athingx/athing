package io.github.athingx.athing.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.util.function.Function;

/**
 * JSON对象工具类
 */
public class JsonObjectUtils {

    /**
     * 是否空属性
     *
     * @param json     json对象
     * @param property 属性名
     * @return TRUE | FALSE
     */
    public static boolean isEmptyProperty(JsonObject json, String property) {

        // 属性不存在则为空
        if (!json.has(property)) {
            return true;
        }

        final var propertyJson = json.get(property);

        // 原生类型是不可能为空
        if (propertyJson.isJsonPrimitive()) {
            return false;
        }

        // 对象类型，判断是否有属性
        if (propertyJson.isJsonObject()) {
            return propertyJson.getAsJsonObject().size() == 0;
        }

        // 数组类型，判断是否有元素
        if (propertyJson.isJsonArray()) {
            return propertyJson.getAsJsonArray().isEmpty();
        }

        // 其他情况下为不空
        return false;
    }

    /**
     * 是否非空属性
     *
     * @param json     json对象
     * @param property 属性名
     * @return TRUE | FALSE
     */
    public static boolean isNotEmptyProperty(JsonObject json, String property) {
        return !isEmptyProperty(json, property);
    }

    public static String requireAsString(JsonObject json, String property) {
        if (!json.has(property)) {
            throw new IllegalArgumentException("require property: " + property);
        }
        return json.get(property).getAsString();
    }

    public static long requireAsLong(JsonObject json, String property) {
        if (!json.has(property)) {
            throw new IllegalArgumentException("require property: " + property);
        }
        return json.get(property).getAsLong();
    }

    public static int requireAsInt(JsonObject json, String property) {
        if (!json.has(property)) {
            throw new IllegalArgumentException("require property: " + property);
        }
        return json.get(property).getAsInt();
    }

    public static URI requireAsUri(JsonObject json, String property) {
        if (!json.has(property)) {
            throw new IllegalArgumentException("require property: " + property);
        }
        return URI.create(json.get(property).getAsString());
    }

    public static String getAsString(JsonObject json, String property, String def) {
        return json.has(property) ? json.get(property).getAsString() : def;
    }

    public static String getAsString(JsonObject json, String property) {
        return getAsString(json, property, null);
    }

    public static long getAsLong(JsonObject json, String property, long def) {
        return json.has(property) ? json.get(property).getAsLong() : def;
    }

    public static long getAsLong(JsonObject json, String property) {
        return getAsLong(json, property, 0L);
    }

    public static <X> X getAsObject(JsonObject json, String property, Function<JsonElement, X> function) {
        return isNotEmptyProperty(json, property)
                ? function.apply(json.get(property))
                : null;
    }

}
