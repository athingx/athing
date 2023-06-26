package io.github.athingx.athing.thing;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.domain.OpReply;
import io.github.athingx.athing.thing.api.op.OpBind;
import io.github.athingx.athing.thing.api.op.OpPost;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.mqtt.AliyunMqttClientFactory;
import io.github.athingx.athing.thing.api.domain.OpMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static io.github.athingx.athing.thing.api.function.CompletableFutureFn.thenComposeOpReply;
import static io.github.athingx.athing.thing.api.function.CompletableFutureFn.whenSuccessfully;
import static io.github.athingx.athing.thing.api.function.ThingFn.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备操作测试用例
 */
public class ThingOpTestCase implements LoadingProperties {

    @Test
    public void test$thing$op_call$success() throws Exception {
        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executorFactory(path -> Executors.newFixedThreadPool(20))
                .clientFactory(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();

        final var thingCall = thing.op().bind(
                        OpPost.topic("/sys/%s/thing/config/get".formatted(thing.path().toURN())),
                        OpBind.newBuilder("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))
                                .decode(mappingByteToJson(UTF_8))
                                .decode(mappingJsonToType(new TypeToken<OpReply<Data>>() {

                                }))
                                .build()
                )
                .get();

        final var token = thing.op().genToken();


        final var data = thingCall.call(new OpMap(token)
                        .putProperty("id", token)
                        .putProperty("version", "1.0")
                        .putProperty("method", "thing.config.get")
                        .putProperty("params", new OpMap(token)
                                .putProperty("configScope", "product")
                                .putProperty("getType", "file")
                        )
                )
                .whenComplete(whenSuccessfully(reply -> Assert.assertTrue(reply.isOk())))
                .whenComplete(whenSuccessfully(reply -> Assert.assertEquals(token, reply.token())))
                .whenComplete(whenSuccessfully(reply -> Assert.assertNotNull(reply.data())))
                .thenCompose(thenComposeOpReply())
                .get();

        Assert.assertNotNull(data.id);
        Assert.assertTrue(data.size > 0);
        Assert.assertNotNull(data.sign);
        Assert.assertNotNull(data.method);
        Assert.assertNotNull(data.url);
        Assert.assertNotNull(data.type);
        thingCall.unbind().get();
        thing.destroy();
    }

    @Test
    public void test$thing$op_bind$success() throws Exception {

        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executorFactory(path -> Executors.newFixedThreadPool(20))
                .clientFactory(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();

        final BlockingQueue<OpReply<Data>> queue = new LinkedBlockingQueue<>();
        final var thingBind = thing.op().bind(
                        OpBind.newBuilder("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))
                                .filter(matchingTopic(topic -> topic.equals("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))))
                                .decode(mappingByteToJson(UTF_8))
                                .decode(mappingJsonToOpReply(Data.class))
                                .build(),
                        (topic, reply) -> {
                            while (true) {
                                if (queue.offer(reply)) {
                                    break;
                                }
                            }
                        }
                )
                .get();

        final String token = thing.op().genToken();
        thing.op().post("/sys/%s/thing/config/get".formatted(thing.path().toURN()),
                        new OpMap(token)
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("params", new OpMap(token)
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )
                )
                .get();

        final OpReply<Data> reply = queue.take();
        Assert.assertEquals(token, reply.token());
        Assert.assertTrue(reply.isOk());
        Assert.assertNotNull(reply.data());
        Assert.assertNotNull(reply.data().id);
        Assert.assertTrue(reply.data().size > 0);
        Assert.assertNotNull(reply.data().sign);
        Assert.assertNotNull(reply.data().method);
        Assert.assertNotNull(reply.data().url);
        Assert.assertNotNull(reply.data().type);

        thingBind.unbind().get();
        thing.destroy();
    }

    // 数据格式
    record Data(
            @SerializedName("configId") String id,
            @SerializedName("configSize") int size,
            @SerializedName("sign") String sign,
            @SerializedName("signMethod") String method,
            @SerializedName("url") String url,
            @SerializedName("getType") String type
    ) {

    }
}
