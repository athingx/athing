package io.github.athingx.athing.thing;

import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.OpMapData;
import io.github.athingx.athing.thing.api.op.OpReply;
import io.github.athingx.athing.thing.api.op.ThingOpBind;
import io.github.athingx.athing.thing.api.util.MapData;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.client.DefaultMqttClientFactory;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;

import static io.github.athingx.athing.thing.api.op.function.OpFunction.identity;
import static io.github.athingx.athing.thing.api.op.function.OpMapper.mappingBytesToJson;
import static io.github.athingx.athing.thing.api.op.function.OpMapper.mappingJsonToOpReply;
import static io.github.athingx.athing.thing.api.util.CompletableFutureUtils.thenComposeOpReply;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备操作测试用例
 */
public class ThingOpTestCase implements LoadingProperties {

    @Test
    public void test$thing$op_call$success() throws Exception {
        final var thing = new ThingBuilder(PRODUCT_ID, THING_ID)
                .client(new DefaultMqttClientFactory()
                        .remote(REMOTE)
                        .secret(SECRET)
                )
                .build();

        final var path = thing.path().toURN();
        final var caller = thing.op().bind("/sys/%s/thing/config/get_reply".formatted(path))
                .map(mappingBytesToJson(UTF_8))
                .map(mappingJsonToOpReply(Data.class))
                .caller(new ThingOpBind.Option(), identity())
                .get();

        final var token = thing.op().genToken();
        final var data = caller.call("/sys/%s/thing/config/get".formatted(path),
                        new OpMapData(token, new MapData()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("sys", prop -> prop
                                        .putProperty("ack", 1)
                                )
                                .putProperty("params", prop -> prop
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )))
                .thenCompose(thenComposeOpReply())
                .get();

        Assert.assertNotNull(data.id);
        Assert.assertTrue(data.size > 0);
        Assert.assertNotNull(data.sign);
        Assert.assertNotNull(data.method);
        Assert.assertNotNull(data.url);
        Assert.assertNotNull(data.type);

        // 销毁
        caller.unbind().get();
        thing.destroy();
    }

    @Test
    public void test$thing$op_bind$success() throws Exception {

        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .client(new DefaultMqttClientFactory()
                        .remote(REMOTE)
                        .secret(SECRET)
                )
                .build();

        final var path = thing.path().toURN();
        final var queue = new LinkedBlockingQueue<OpReply<Data>>();

        final var binder = thing.op().bind("/sys/%s/thing/config/get_reply".formatted(path))
                .filter((topic, data) -> true)
                .map(mappingBytesToJson(UTF_8))
                .map(mappingJsonToOpReply(Data.class))
                .consumer((topic, reply) -> {
                    while (true) {
                        if (queue.offer(reply)) {
                            break;
                        }
                    }
                })
                .get();

        final var token = thing.op().genToken();
        thing.op().post("/sys/%s/thing/config/get".formatted(path),
                        new OpMapData(token, new MapData()
                                .putProperty("id", token)
                                .putProperty("version", "1.0")
                                .putProperty("method", "thing.config.get")
                                .putProperty("sys", prop -> prop
                                        .putProperty("ack", 1)
                                )
                                .putProperty("params", prop -> prop
                                        .putProperty("configScope", "product")
                                        .putProperty("getType", "file")
                                )))
                .get();

        final OpReply<Data> reply = queue.take();
        Assert.assertEquals(token, reply.token());
        Assert.assertTrue(reply.isSuccess());
        Assert.assertNotNull(reply.data());
        Assert.assertNotNull(reply.data().id);
        Assert.assertTrue(reply.data().size > 0);
        Assert.assertNotNull(reply.data().sign);
        Assert.assertNotNull(reply.data().method);
        Assert.assertNotNull(reply.data().url);
        Assert.assertNotNull(reply.data().type);

        binder.unbind().get();
        thing.destroy();
    }

    // 数据格式
    record Data(
            @SerializedName("configId") String id,
            @SerializedName("configSize") int size,
            @SerializedName("sign") String sign,
            @SerializedName("signMethod") String method,
            @SerializedName("url") String url,
            @SerializedName("getType") String type) {

    }

    @Test
    public void test() {
        final var uri = URI.create("mqtt://abcdefg/12345");
        System.out.println(uri.getHost());
        System.out.println(uri.getPath().replaceFirst("/", ""));
    }

}
