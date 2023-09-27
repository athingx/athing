package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.*;
import io.github.athingx.athing.thing.impl.util.TokenSequencer;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

/**
 * 设备操作实现
 */
public class ThingOpImpl<T, R> extends MqttClientSupport implements ThingOp<T, R> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingPath path;
    private final IMqttAsyncClient client;
    private final ExecutorService executor;
    private final Codec<byte[], byte[], T, R> codec;
    private final TokenSequencer sequencer = new TokenSequencer();


    /**
     * 设备操作实现
     *
     * @param path     设备路径
     * @param client   MQTT客户端
     * @param executor 线程池
     */
    public ThingOpImpl(ThingPath path, IMqttAsyncClient client, ExecutorService executor, Codec<byte[], byte[], T, R> codec) {
        super(client);
        this.path = path;
        this.client = client;
        this.executor = executor;
        this.codec = codec;
    }

    @Override
    public String genToken() {
        return sequencer.next();
    }

    @Override
    public CompletableFuture<Void> post(String topic, T data) {
        return CompletableFuture
                .supplyAsync(() -> codec.encoder().encode(data), executor)
                .thenCompose(bytes -> pahoMqttPublish(topic, 1, bytes));
    }

    @Override
    public CompletableFuture<OpBinder> consumer(String express, BiConsumer<String, R> consumer) {
        return pahoMqttSubscribe(express, 1, (topic, message) ->
                executor.execute(() -> {
                    try {
                        Optional.ofNullable(codec.decoder().decode(topic, message.getPayload()))
                                .ifPresentOrElse(
                                        data -> consumer.accept(topic, data),
                                        () -> logger.debug("{}/op message decode none, ignored! topic={};", path, topic)
                                );
                    } catch (Throwable ex) {
                        logger.warn("{}/op message consume error! topic={};", path, topic, ex);
                    }
                }))
                .thenApply(v -> () -> pahoMqttUnsubscribe(express));
    }

    @Override
    public <UT extends OpData, UR extends OpData>
    CompletableFuture<OpTopicCaller<UT, UR>> caller(String express, Codec<T, R, UT, UR> codec) {
        final var impl = codec(codec);
        final var futureMap = new ConcurrentHashMap<String, CompletableFuture<UR>>();
        return impl.consumer(express, (topic, ur) -> {

                    // 获取操作
                    final var future = futureMap.remove(ur.token());
                    if (null == future) {
                        logger.debug("{}/op/call maybe timeout! token={};topic={};", path, ur.token(), topic);
                        return;
                    }

                    // 完成操作
                    if (!future.complete(ur)) {
                        logger.debug("{}/op/call maybe expired! token={};topic={};", path, ur.token(), topic);
                    }

                })
                .thenApply(consumer -> new OpTopicCaller<UT, UR>() {
                    @Override
                    public CompletableFuture<UR> call(String topic, UT ut) {
                        final var future = new CompletableFuture<UR>();
                        futureMap.put(ut.token(), future);
                        impl.post(topic, ut).whenComplete((r, ex) -> ofNullable(ex).ifPresent(future::completeExceptionally));
                        return future.whenComplete((r, ex) -> futureMap.remove(ut.token()));
                    }

                    @Override
                    public CompletableFuture<Void> unbind() {
                        return consumer.unbind();
                    }
                });
    }

    @Override
    public <UT, UR> ThingOp<UT, UR> codec(Codec<T, R, UT, UR> codec) {
        return new ThingOpImpl<>(path, client, executor, this.codec.chain(codec));
    }

}
