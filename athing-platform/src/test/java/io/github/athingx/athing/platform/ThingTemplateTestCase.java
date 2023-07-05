package io.github.athingx.athing.platform;

import com.aliyuncs.v5.iot.model.v20180120.QueryDeviceDetailRequest;
import com.aliyuncs.v5.iot.model.v20180120.QueryDeviceDetailResponse;
import io.github.athingx.athing.platform.api.ThingPlatformException;
import io.github.athingx.athing.platform.api.ThingTemplate;
import io.github.athingx.athing.platform.api.client.ThingPlatformClient;
import io.github.athingx.athing.platform.builder.ThingPlatformBuilder;
import io.github.athingx.athing.platform.builder.client.AliyunThingPlatformClientFactory;
import org.junit.Assert;
import org.junit.Test;

public class ThingTemplateTestCase implements LoadingProperties {

    interface ThingMgrTemplate extends ThingTemplate {

        QueryDeviceDetailResponse getDetail() throws ThingPlatformException;

    }

    static class ThingMgrTemplateImpl implements ThingMgrTemplate {

        private final ThingPlatformClient client;
        private final String productId;
        private final String thingId;

        ThingMgrTemplateImpl(ThingPlatformClient client, String productId, String thingId) {
            this.client = client;
            this.productId = productId;
            this.thingId = thingId;
        }

        @Override
        public QueryDeviceDetailResponse getDetail() throws ThingPlatformException {
            final QueryDeviceDetailRequest request = new QueryDeviceDetailRequest();
            request.setProductKey(productId);
            request.setDeviceName(thingId);
            return client.execute(request, request.getResponseClass());
        }

    }

    @Test
    public void platform$template$success() throws Exception {
        final var platform = new ThingPlatformBuilder()
                .clientFactory(new AliyunThingPlatformClientFactory()
                        .region("cn-shanghai")
                        .identity(PLATFORM_IDENTITY)
                        .secret(PLATFORM_SECRET)
                )
                .build();

        platform.register(ThingMgrTemplate.class, ThingMgrTemplateImpl::new);
        final ThingMgrTemplate thingMgr = platform.genThingTemplate(ThingMgrTemplate.class, PRODUCT_ID, THING_ID);
        final QueryDeviceDetailResponse response = thingMgr.getDetail();
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getSuccess());
        Assert.assertNotNull(response.getData());

        platform.destroy();
    }

}
