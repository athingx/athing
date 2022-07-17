package io.github.athingx.athing.platform.builder.client;

import com.aliyun.teaopenapi.models.Config;
import io.github.athingx.athing.platform.api.client.ThingClient;

import static java.util.Objects.requireNonNull;

/**
 * 阿里云设备客户端工厂实现
 */
public class AliyunThingClientFactory implements ThingClientFactory {

    private String identity;
    private String secret;
    private String region = "cn-shanghai";

    /**
     * 账号
     *
     * @param identity 账号
     * @return this
     */
    public AliyunThingClientFactory identity(String identity) {
        this.identity = identity;
        return this;
    }

    /**
     * 密码
     *
     * @param secret 密码
     * @return this
     */
    public AliyunThingClientFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * 区域
     *
     * @param region 区域
     * @return this
     */
    public AliyunThingClientFactory region(String region) {
        this.region = region;
        return this;
    }

    @Override
    public ThingClient make() throws Exception {

        requireNonNull(identity, "identity is required!");
        requireNonNull(secret, "secret is required!");
        requireNonNull(region, "region is required!");

        final Config config = new Config();
        config.setAccessKeyId(identity);
        config.setAccessKeySecret(secret);
        config.setRegionId(region);
        return new ThingClient(config);
    }

}
