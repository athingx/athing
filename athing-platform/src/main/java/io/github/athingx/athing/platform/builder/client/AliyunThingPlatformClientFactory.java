package io.github.athingx.athing.platform.builder.client;

import com.aliyuncs.v5.profile.DefaultProfile;
import io.github.athingx.athing.platform.api.client.ThingPlatformClient;
import io.github.athingx.athing.platform.impl.client.ThingPlatformClientImpl;

import static java.util.Objects.requireNonNull;

/**
 * 设备平台客户端（阿里云）工厂
 */
public class AliyunThingPlatformClientFactory implements ThingPlatformClientFactory {

    private String identity;
    private String secret;
    private String region = "cn-shanghai";

    /**
     * 账号
     *
     * @param identity 账号
     * @return this
     */
    public AliyunThingPlatformClientFactory identity(String identity) {
        this.identity = identity;
        return this;
    }

    /**
     * 密码
     *
     * @param secret 密码
     * @return this
     */
    public AliyunThingPlatformClientFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * 区域
     *
     * @param region 区域
     * @return this
     */
    public AliyunThingPlatformClientFactory region(String region) {
        this.region = region;
        return this;
    }

    @Override
    public ThingPlatformClient make() {
        return new ThingPlatformClientImpl(
                DefaultProfile.getProfile(
                        requireNonNull(region, "region is required!"),
                        requireNonNull(identity, "identity is required!"),
                        requireNonNull(secret, "secret is required!")
                )
        );
    }

}
