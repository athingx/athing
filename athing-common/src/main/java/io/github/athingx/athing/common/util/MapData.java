package io.github.athingx.athing.common.util;

import java.util.HashMap;

/**
 * Map对象
 * <p>
 * 集联操作，便于构造一个对象进行Json序列化
 * </p>
 */
public class MapData extends HashMap<String, Object> {

    private final transient MapData parent;

    public MapData() {
        this(null);
    }

    private MapData(MapData parent) {
        this.parent = parent;
    }

    public MapData putProperty(String name, Object value) {
        put(name, value);
        return this;
    }

    public MapData enterProperty(String name) {
        final MapData mapData = new MapData(this);
        put(name, mapData);
        return mapData;
    }

    public MapData exitProperty() {
        if (null == parent) {
            throw new IllegalStateException("root");
        }
        return parent;
    }

}
