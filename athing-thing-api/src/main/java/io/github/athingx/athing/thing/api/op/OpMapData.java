package io.github.athingx.athing.thing.api.op;

import io.github.athingx.athing.thing.api.util.MapData;

/**
 * 操作数据
 */
public class OpMapData extends MapData implements OpData {

    private transient final String token;

    /**
     * 构造操作数据
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
