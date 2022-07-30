package io.github.athingx.athing.thing;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.OpReply;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.mqtt.AliyunMqttClientFactory;
import io.github.athingx.athing.thing.util.MapData;
import io.github.athingx.athing.thing.util.MapOpData;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static io.github.athingx.athing.thing.api.function.ThingFn.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备操作测试用例
 */
public class ThingOpTestCase implements LoadingProperties {

    @Test
    public void test$thing$op_call$success() throws Exception {
        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();

        final var caller = thing.op()
                .bind("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))
                .matches(matchingTopic(topic -> topic.equals("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))))
                .map(mappingJsonFromByte(UTF_8))
                .map(mappingJsonToType(new TypeToken<OpReply<Data>>() {

                }))
                .call(identity())
                .get();

        final String token = thing.op().genToken();
        final OpReply<Data> reply = caller.call("/sys/%s/thing/config/get".formatted(thing.path().toURN()),
                        new MapOpData(token, new MapData()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("params", new MapData()
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )
                        ))
                .get();

        Assert.assertEquals(token, reply.token());
        Assert.assertTrue(reply.isOk());
        Assert.assertNotNull(reply.data());
        Assert.assertNotNull(reply.data().id);
        Assert.assertTrue(reply.data().size > 0);
        Assert.assertNotNull(reply.data().sign);
        Assert.assertNotNull(reply.data().method);
        Assert.assertNotNull(reply.data().url);
        Assert.assertNotNull(reply.data().type);
        caller.unbind();
        thing.destroy();
    }

    @Test
    public void test$thing$op_bind$success() throws Exception {

        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();

        final BlockingQueue<OpReply<Data>> queue = new LinkedBlockingQueue<>();
        final var binder = thing.op().bind("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))
                .matches(matchingTopic(topic -> topic.equals("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))))
                .map(mappingJsonFromByte(UTF_8))
                .map(mappingJsonToOpReply(Data.class))
                .bind((topic, reply) -> {
                    while (true) {
                        if (queue.offer(reply)) {
                            break;
                        }
                    }
                })
                .get();

        final String token = thing.op().genToken();
        thing.op().data("/sys/%s/thing/config/get".formatted(thing.path().toURN()),
                        new MapOpData(token, new MapData()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("params", new MapData()
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )
                        ))
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

        binder.unbind();
        thing.destroy();
    }


    @Test
    public void test$thing$op_group_call$success() throws Exception {
        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();

        final var group = thing.op().group();

        final var callerF = group
                .bind("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))
                .matches(matchingTopic(topic -> topic.equals("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))))
                .map(mappingJsonFromByte(UTF_8))
                .map(mappingJsonToOpReply(Data.class))
                .call(identity());

        final var binder = group.commit().get();
        final var caller = callerF.get();

        final String token = thing.op().genToken();
        final OpReply<Data> reply = caller.call("/sys/%s/thing/config/get".formatted(thing.path().toURN()),
                        new MapOpData(token, new MapData()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("params", new MapData()
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )
                        ))
                .get();

        Assert.assertEquals(token, reply.token());
        Assert.assertTrue(reply.isOk());
        Assert.assertNotNull(reply.data());
        Assert.assertNotNull(reply.data().id);
        Assert.assertTrue(reply.data().size > 0);
        Assert.assertNotNull(reply.data().sign);
        Assert.assertNotNull(reply.data().method);
        Assert.assertNotNull(reply.data().url);
        Assert.assertNotNull(reply.data().type);
        binder.unbind();
        thing.destroy();
    }

    @Test
    public void test$thing$op_group_bind$success() throws Exception {

        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executor(path -> Executors.newFixedThreadPool(20))
                .client(new AliyunMqttClientFactory()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();


        final BlockingQueue<OpReply<Data>> queue = new LinkedBlockingQueue<>();

        final var group = thing.op().group();
        group.bind("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))
                .matches(matchingTopic(topic -> topic.equals("/sys/%s/thing/config/get_reply".formatted(thing.path().toURN()))))
                .map(mappingJsonFromByte(UTF_8))
                .map(mappingJsonToOpReply(Data.class))
                .bind((topic, reply) -> {
                    while (true) {
                        if (queue.offer(reply)) {
                            break;
                        }
                    }
                });
        final var binder = group.commit().get();

        final String token = thing.op().genToken();
        thing.op().data("/sys/%s/thing/config/get".formatted(thing.path().toURN()),
                        new MapOpData(token, new MapData()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("params", new MapData()
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )
                        ))
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

        binder.unbind();
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
