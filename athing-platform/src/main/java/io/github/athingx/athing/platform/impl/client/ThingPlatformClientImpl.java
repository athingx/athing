package io.github.athingx.athing.platform.impl.client;

import com.aliyuncs.v5.AcsRequest;
import com.aliyuncs.v5.DefaultAcsClient;
import com.aliyuncs.v5.IAcsClient;
import com.aliyuncs.v5.profile.IClientProfile;
import io.github.athingx.athing.platform.api.ThingPlatformException;
import io.github.athingx.athing.platform.api.client.ThingPlatformClient;

import java.util.Objects;

public class ThingPlatformClientImpl implements ThingPlatformClient {

    private final IAcsClient client;

    public ThingPlatformClientImpl(IClientProfile profile) {
        this.client = new DefaultAcsClient(profile);
        client.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R> R execute(T request, Class<R> responseClass) throws ThingPlatformException {

        // 检查请求是否为AcsRequest
        if (!(request instanceof AcsRequest<?> acsRequest)) {
            throw new ThingPlatformException("request must be AcsRequest");
        }

        // 检查Response类型是否与请求匹配
        if (!Objects.equals(acsRequest.getResponseClass(), responseClass)) {
            throw new ThingPlatformException("responseClass must be %s".formatted(acsRequest.getResponseClass()));
        }

        // 执行请求
        try {
            return (R) client.getAcsResponse(acsRequest);
        } catch (Exception cause) {
            throw new ThingPlatformException(cause);
        }

    }

    @Override
    public void shutdown() {
        client.shutdown();
    }

}
