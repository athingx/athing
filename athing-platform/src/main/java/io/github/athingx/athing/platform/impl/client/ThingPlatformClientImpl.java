package io.github.athingx.athing.platform.impl.client;

import com.aliyuncs.v5.AcsRequest;
import com.aliyuncs.v5.DefaultAcsClient;
import com.aliyuncs.v5.IAcsClient;
import com.aliyuncs.v5.profile.IClientProfile;
import io.github.athingx.athing.platform.api.ThingPlatformException;
import io.github.athingx.athing.platform.api.client.ThingPlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 设备平台客户端实现(阿里云)
 */
public class ThingPlatformClientImpl implements ThingPlatformClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final IAcsClient client;
    private final String _string;

    /**
     * 设备平台客户端实现
     *
     * @param profile 阿里云客户端连接配置
     */
    public ThingPlatformClientImpl(IClientProfile profile) {
        this.client = new DefaultAcsClient(profile);
        this._string = "thing-platform-client://%s".formatted(profile.getRegionId());
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
        logger.debug("{} shutdown!", this);
    }

    @Override
    public String toString() {
        return _string;
    }

}
