package io.github.athingx.athing.platform.builder.client;

import com.aliyuncs.v5.DefaultAcsClient;
import com.aliyuncs.v5.IAcsClient;
import com.aliyuncs.v5.http.HttpClientConfig;
import com.aliyuncs.v5.profile.DefaultProfile;
import com.aliyuncs.v5.profile.IClientProfile;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * 阿里云设备客户端工厂实现
 */
public class AliyunIAcsClientFactory implements IAcsClientFactory {

    private String identity;
    private String secret;
    private String region = "cn-shanghai";
    private HttpClientConfig config = new HttpClientConfig();

    /**
     * 账号
     *
     * @param identity 账号
     * @return this
     */
    public AliyunIAcsClientFactory identity(String identity) {
        this.identity = identity;
        return this;
    }

    /**
     * 密码
     *
     * @param secret 密码
     * @return this
     */
    public AliyunIAcsClientFactory secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * 区域
     *
     * @param region 区域
     * @return this
     */
    public AliyunIAcsClientFactory region(String region) {
        this.region = region;
        return this;
    }

    /**
     * 配置
     *
     * @param config 配置
     * @return this
     */
    public AliyunIAcsClientFactory config(HttpClientConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public IAcsClient make() {

        final IClientProfile profile = DefaultProfile.getProfile(
                requireNonNull(region, "region is required!"),
                requireNonNull(identity, "identity is required!"),
                requireNonNull(secret, "secret is required!")
        );

        if (Objects.nonNull(config)) {
            profile.setHttpClientConfig(config);
        }

        return new DefaultAcsClient(profile);
    }

}
