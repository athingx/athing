package io.github.athingx.athing.platform;

import io.github.athingx.athing.platform.api.message.ThingLifeCycleMessage;
import io.github.athingx.athing.platform.api.message.ThingMessage;
import io.github.athingx.athing.platform.api.message.ThingStateMessage;
import io.github.athingx.athing.platform.api.message.decoder.DecodeException;
import io.github.athingx.athing.platform.api.message.decoder.ThingLifeCycleMessageDecoder;
import io.github.athingx.athing.platform.api.message.decoder.ThingStateMessageDecoder;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Stream;

public class ThingMessageTestCase implements LoadingProperties {

    @Test
    public void thing$message$decode$lifecycle() {

        final Queue<ThingMessage> queue = new LinkedList<>();

        Stream.of(new ThingLifeCycleMessageDecoder().decode(
                UUID.randomUUID().toString(),
                "/%s/%s/thing/lifecycle".formatted(PRODUCT_ID, THING_ID),
                """
                        {
                            "action": "enable",
                            "iotId": "4z819VQHk6VSLmmBJfrf00107e1234",
                            "productKey": "%s",
                            "deviceName": "%s",
                            "deviceSecret": "%s",
                            "messageCreateTime": 1510292739881
                        }
                        """.formatted(PRODUCT_ID, THING_ID, SECRET)
        )).forEach(queue::offer);

        final ThingLifeCycleMessage message = (ThingLifeCycleMessage) queue.poll();
        Assert.assertNotNull(message);
        Assert.assertEquals(PRODUCT_ID, message.getProductId());
        Assert.assertEquals(THING_ID, message.getThingId());
        Assert.assertEquals(ThingLifeCycleMessage.LifeCycle.ENABLE, message.getLifeCycle());
        Assert.assertEquals(1510292739881L, message.getTimestamp());

    }

    @Test
    public void thing$message$decode$state() throws DecodeException {

        final Queue<ThingMessage> queue = new LinkedList<>();

        Stream.of(new ThingStateMessageDecoder().decode(
                UUID.randomUUID().toString(),
                "/as/mqtt/status/%s/%s".formatted(PRODUCT_ID, THING_ID),
                """
                        {
                             "status":"online",
                             "iotId":"4z819VQHk6VSLmmBJfrf00107e1234",
                             "productKey":"%s",
                             "deviceName":"%s",
                             "time":"2018-08-31 15:32:28.205",
                             "utcTime":"2018-08-31T07:32:28.205Z",
                             "lastTime":"2018-08-31 15:32:28.195",
                             "utcLastTime":"2018-08-31T07:32:28.195Z",
                             "clientIp":"192.0.2.1"
                         }
                        """.formatted(PRODUCT_ID, THING_ID)
        )).forEach(queue::offer);

        final ThingStateMessage message = (ThingStateMessage) queue.poll();
        Assert.assertNotNull(message);
        Assert.assertEquals(PRODUCT_ID, message.getProductId());
        Assert.assertEquals(THING_ID, message.getThingId());
        Assert.assertEquals(ThingStateMessage.State.ONLINE, message.getState());
        Assert.assertEquals("192.0.2.1", message.getLastOnlineIp());
        Assert.assertTrue(message.getTimestamp() > 0);
        Assert.assertTrue(message.getLastOnlineTimestamp() > 0);
    }

}
