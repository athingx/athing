package io.github.athingx.athing.thing.api.domain;

import io.github.athingx.athing.thing.api.op.OpData;

import java.util.HashMap;

/**
 * Map对象
 * <p>
 * 集联操作，便于构造一个对象进行Json序列化
 * </p>
 */
public class OpMap extends HashMap<String, Object> implements OpData {

    private final String token;

    public OpMap(String token) {
        this.token = token;
    }

    public OpMap putProperty(String name, Object value) {
        put(name, value);
        return this;
    }

    @Override
    public String token() {
        return token;
    }

}
