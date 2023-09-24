package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.util.MapData;

import java.util.HashMap;

/**
 * 操作数据
 */
public class OpMapData extends HashMap<String, Object> implements OpData {

    private final String token;

    /**
     * 操作数据
     *
     * @param token 操作令牌
     * @param map   数据
     */
    public OpMapData(String token, MapData map) {
        this.token = token;
        putAll(map);
    }

    @Override
    public String token() {
        return token;
    }

}
