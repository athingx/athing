package io.github.athingx.athing.platform.builder.client;

import com.aliyuncs.v5.DefaultAcsClient;
import com.aliyuncs.v5.IAcsClient;
import com.aliyuncs.v5.profile.DefaultProfile;
import com.aliyuncs.v5.profile.IClientProfile;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * 阿里云设备客户端工厂实现
 */
public class AliyunIAcsClientFactory implements IAcsClientFactory {

    private String identity;
    private String secret;
    private String region = "cn-shanghai";
    private Function<IClientProfile, IClientProfile> profileFn = Function.identity();

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
     * @param profileFn 配置函数
     * @return this
     */
    public AliyunIAcsClientFactory profile(Function<IClientProfile, IClientProfile> profileFn) {
        this.profileFn = profileFn;
        return this;
    }

    @Override
    public IAcsClient make() {

        final IClientProfile profile = DefaultProfile.getProfile(
                requireNonNull(region, "region is required!"),
                requireNonNull(identity, "identity is required!"),
                requireNonNull(secret, "secret is required!")
        );

        return new DefaultAcsClient(profileFn.apply(profile));
    }

}
