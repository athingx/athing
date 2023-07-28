package io.github.athingx.athing.thing;

import com.google.gson.annotations.SerializedName;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.PubPort;
import io.github.athingx.athing.thing.api.op.SubPort;
import io.github.athingx.athing.thing.api.op.domain.OpRequest;
import io.github.athingx.athing.thing.api.op.domain.OpResponse;
import io.github.athingx.athing.thing.api.op.function.OpDecoder;
import io.github.athingx.athing.thing.api.op.function.OpEncoder;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.mqtt.MqttClientFactoryImplByAliyun;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static io.github.athingx.athing.thing.api.util.CompletableFutureUtils.thenComposeOpReply;
import static io.github.athingx.athing.thing.api.util.CompletableFutureUtils.whenSuccessfully;
import static io.github.athingx.athing.thing.builder.mqtt.MqttConnectStrategy.alwaysReTry;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备操作测试用例
 */
public class ThingOpTestCase implements LoadingProperties {

    @Test
    public void test$thing$op_call$success() throws Exception {
        final var thing = new ThingBuilder(PRODUCT_ID, THING_ID)
                .clientFactory(new MqttClientFactoryImplByAliyun()
                        .secret(SECRET)
                        .remote(REMOTE)
                        .strategy(alwaysReTry())
                )
                .build();

        final var path = thing.path().toURN();

        final var caller = thing.op()
                .caller(PubPort.topic("/sys/%s/thing/config/get".formatted(path))
                                .encode(OpEncoder.encodeByteFromJson(UTF_8))
                                .encode(OpEncoder.encodeJsonFromOpRequest(ReqData.class)),
                        SubPort.express("/sys/%s/thing/config/get_reply".formatted(path))
                                .decode(OpDecoder.decodeByteToJson(UTF_8))
                                .decode(OpDecoder.decodeJsonToOpResponse(RespData.class)))
                .get();

        final var response = caller.call((topic, token) -> OpRequest.of(token, "thing.config.get", new ReqData("product", "file")))
                .whenComplete(whenSuccessfully(reply -> Assert.assertTrue(reply.isOk())))
                .whenComplete(whenSuccessfully(reply -> Assert.assertNotNull(reply.data())))
                .thenCompose(thenComposeOpReply())
                .get();

        Assert.assertNotNull(response.id);
        Assert.assertTrue(response.size > 0);
        Assert.assertNotNull(response.sign);
        Assert.assertNotNull(response.method);
        Assert.assertNotNull(response.url);
        Assert.assertNotNull(response.type);

        // 销毁
        caller.unbind().get();
        thing.destroy();
    }

    @Test
    public void test$thing$op_bind$success() throws Exception {

        final var thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .executorFactory(path -> Executors.newFixedThreadPool(20))
                .clientFactory(new MqttClientFactoryImplByAliyun()
                        .secret(SECRET)
                        .remote(REMOTE)
                )
                .build();

        final var path = thing.path().toURN();
        final var queue = new LinkedBlockingQueue<OpResponse<RespData>>();
        final var binder = thing.op()
                .consumer(SubPort.express("/sys/%s/thing/config/get_reply".formatted(path))
                                .decode(OpDecoder.filter((topic, data) -> topic.equals("/sys/%s/thing/config/get_reply".formatted(path))))
                                .decode(OpDecoder.decodeByteToJson(UTF_8))
                                .decode(OpDecoder.decodeJsonToOpResponse(RespData.class)),
                        (topic, response) -> {
                            while (true) {
                                if (queue.offer(response)) {
                                    break;
                                }
                            }
                        })
                .get();


        final var poster = thing.op()
                .poster(PubPort.topic("/sys/%s/thing/config/get".formatted(path))
                        .encode(OpEncoder.encodeByteFromJson(UTF_8))
                        .encode(OpEncoder.encodeJsonFromOpRequest(ReqData.class)))
                .get();

        final var request = poster.post((token, data) -> OpRequest.of(token, "thing.config.get", new ReqData("product", "file")))
                .get();

        final OpResponse<RespData> response = queue.take();
        Assert.assertEquals(request.token(), response.token());
        Assert.assertTrue(response.isOk());
        Assert.assertNotNull(response.data());
        Assert.assertNotNull(response.data().id);
        Assert.assertTrue(response.data().size > 0);
        Assert.assertNotNull(response.data().sign);
        Assert.assertNotNull(response.data().method);
        Assert.assertNotNull(response.data().url);
        Assert.assertNotNull(response.data().type);

        binder.unbind().get();
        poster.unbind().get();
        thing.destroy();
    }

    record ReqData(
            @SerializedName("configScope") String scope,
            @SerializedName("getType") String type) {
    }

    // 数据格式
    record RespData(
            @SerializedName("configId") String id,
            @SerializedName("configSize") int size,
            @SerializedName("sign") String sign,
            @SerializedName("signMethod") String method,
            @SerializedName("url") String url,
            @SerializedName("getType") String type
    ) {

    }

}
