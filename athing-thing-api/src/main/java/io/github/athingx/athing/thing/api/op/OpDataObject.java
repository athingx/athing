package io.github.athingx.athing.thing.api.op;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * 操作数据对象
 * <p>
 * 构造一个操作数据对象，用于向设备端发送操作请求
 */
public class OpDataObject extends HashMap<String, Object> implements OpData {

    private transient final String token;

    /**
     * 操作数据对象
     *
     * @param token 操作令牌
     */
    public OpDataObject(String token) {
        this.token = token;
    }

    /**
     * 添加属性
     *
     * @param name  属性名
     * @param value 属性值
     * @return this
     */
    public OpDataObject putProperty(String name, Object value) {
        put(name, value);
        return this;
    }

    /**
     * 添加属性
     *
     * @param name     属性名
     * @param consumer 属性值函数
     * @return this
     */
    public OpDataObject putProperty(String name, Consumer<OpDataObject> consumer) {
        final OpDataObject propertyDataObject = new OpDataObject(token);
        consumer.accept(propertyDataObject);
        put(name, propertyDataObject);
        return this;
    }

    @Override
    public String token() {
        return token;
    }

}
