package io.github.athingx.athing.thing.api.util;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Map数据
 */
public class MapData extends HashMap<String, Object> {

    /**
     * 添加属性
     *
     * @param name  属性名
     * @param value 属性值
     * @return this
     */
    public MapData putProperty(String name, Object value) {
        put(name, value);
        return this;
    }

    /**
     * 添加属性
     *
     * @param name       属性名
     * @param propertyFn 属性处理函数
     * @return this
     */
    public MapData putProperty(String name, Consumer<MapData> propertyFn) {
        final MapData property = new MapData();
        propertyFn.accept(property);
        put(name, property);
        return this;
    }

}
