package io.github.athingx.athing.platform.api.client;

import com.aliyun.iot20180120.Client;
import com.aliyun.teaopenapi.models.Config;

/**
 * 设备客户端
 */
public class ThingClient extends Client {

    private final String _string;

    public ThingClient(Config config) throws Exception {
        super(config);
        this._string = "thing-client://%s".formatted(config.accessKeyId);
    }

    @Override
    public String toString() {
        return _string;
    }

}
