package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.*;
import io.github.athingx.athing.thing.api.op.function.OpConsumer;
import io.github.athingx.athing.thing.api.op.function.OpFunction;
import io.github.athingx.athing.thing.api.op.function.OpPredicate;
import io.github.athingx.athing.thing.impl.util.TokenSequencer;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 设备操作实现
 */
public class ThingOpImpl extends MqttClientSupport implements ThingOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingPath path;
    private final ExecutorService executor;
    private final TokenSequencer sequencer = new TokenSequencer();

    /**
     * 设备操作实现
     *
     * @param path     设备路径
     * @param client   MQTT客户端
     * @param executor 线程池
     */
    public ThingOpImpl(ThingPath path, IMqttAsyncClient client, ExecutorService executor) {
        super(client);
        this.path = path;
        this.executor = executor;
    }

    @Override
    public String genToken() {
        return sequencer.next();
    }

    @Override
    public CompletableFuture<Void> post(String topic, OpData data) {
        return pahoMqttPublish(topic, 1, JsonHelper.toJson(data).getBytes(UTF_8))
                .whenComplete((v, ex)
                        -> logger.debug("{}/op/post completed, token={};topic={};", path, data.token(), topic, ex));
    }

    @Override
    public ThingOpBind<byte[]> bind(String express) {
        return new ThingOpBindImpl<>(express, (topic, data) -> data);
    }


    /**
     * 操作绑定实现
     *
     * @param <V> 绑定数据类型
     */
    private class ThingOpBindImpl<V> implements ThingOpBind<V> {

        private final String express;
        private final OpFunction<byte[], V> mapper;

        private ThingOpBindImpl(String express, OpFunction<byte[], V> mapper) {
            this.express = express;
            this.mapper = mapper;
        }

        private static class SkipException extends RuntimeException {
        }

        @Override
        public ThingOpBind<V> filter(OpPredicate<? super V> filter) {
            return new ThingOpBindImpl<>(express, (topic, data) -> {
                final V value = mapper.apply(topic, data);
                if (!filter.test(topic, value)) {
                    throw new SkipException();
                }
                return value;
            });
        }

        @Override
        public <R> ThingOpBind<R> map(OpFunction<? super V, ? extends R> mapper) {
            return new ThingOpBindImpl<>(express, this.mapper.then(mapper));
        }

        @Override
        public CompletableFuture<ThingOpBinder> consumer(OpConsumer<? super V> consumer) {

            // 绑定操作
            final ThingOpBinder binder = () -> pahoMqttUnsubscribe(express)
                    .whenComplete((v, ex)
                            -> logger.debug("{}/op/consumer unbind completed, express={}", path, express));


            return pahoMqttSubscribe(express, 1, (topic, message) ->

                    // 消费数据
                    executor.execute(() -> {

                        try {

                            // 解码数据
                            final V data = mapper.apply(topic, message.getPayload());

                            // 消费数据
                            consumer.accept(topic, data);

                        } catch (SkipException cause) {
                            logger.debug("{}/op/consumer skipped! topic={};", path, topic);
                        } catch (Throwable cause) {
                            logger.warn("{}/op/consumer failure! topic={};", path, topic, cause);
                        }

                    }))

                    // 转换为绑定操作
                    .thenApply(v -> binder)

                    // 绑定成功或失败都要记录日志
                    .whenComplete((v, ex)
                            -> logger.debug("{}/op/consumer bind completed, express={};", path, express));

        }

        @Override
        public <P extends OpData, R extends OpData> CompletableFuture<ThingOpRouteCaller<P, R>> caller(Option option, OpFunction<? super V, ? extends R> mapper) {
            final var futureMap = new ConcurrentHashMap<String, CompletableFuture<R>>();
            return pahoMqttSubscribe(express, 1, (topic, message) ->

                    // 消费数据
                    executor.execute(() -> {

                        try {

                            final var data = ThingOpBindImpl.this.mapper.then(mapper).apply(topic, message.getPayload());
                            final var token = data.token();
                            final var future = futureMap.remove(token);

                            // future超时
                            if (Objects.isNull(future)) {
                                logger.warn("{}/op/caller/response token not found, maybe timeout! token={};topic={};", path, token, topic);
                                return;
                            }

                            // 记录收到应答
                            logger.debug("{}/op/caller/response received! token={};topic={};", path, token, topic);

                            // future过期
                            if (!future.complete(data)) {
                                logger.warn("{}/op/caller/response assert failure, maybe expired! token={};topic={};", path, token, topic);
                            }

                        } catch (SkipException cause) {
                            logger.debug("{}/op/caller/response skipped! topic={};", path, topic);
                        } catch (Throwable cause) {
                            logger.warn("{}/op/caller/response failure! topic={};", path, topic, cause);
                        }

                    }))

                    // 转换ThingOpRouteCaller
                    .<ThingOpRouteCaller<P, R>>thenApply(unused -> new ThingOpRouteCaller<>() {
                        @Override
                        public CompletableFuture<R> call(String topic, P data) {

                            // call存根
                            final var future = new CompletableFuture<R>();
                            future.orTimeout(option.getTimeoutMs(), TimeUnit.MILLISECONDS)
                                    .whenComplete((v, ex) -> {
                                        futureMap.remove(data.token());
                                        logger.debug("{}/op/caller completed, token={};", path, data.token(), ex);
                                    });

                            // 记录存根
                            futureMap.put(data.token(), future);

                            // 发送请求
                            final var requestF = pahoMqttPublish(topic, 1, JsonHelper.toJson(data).getBytes(UTF_8))
                                    .whenComplete((v, ex)
                                            -> logger.debug("{}/op/caller/request completed, token={};topic={};", path, data.token(), topic, ex));

                            // 合并请求
                            return future.thenCombine(requestF, (r, v) -> r);
                        }

                        @Override
                        public CompletableFuture<Void> unbind() {
                            return pahoMqttUnsubscribe(express)
                                    .whenComplete((v, ex)
                                            -> logger.debug("{}/op/caller unbind completed, express={};", path, express, ex));
                        }

                    })

                    // 绑定成功或失败都要记录日志
                    .whenComplete((v, ex)
                            -> logger.debug("{}/op/caller bind completed, express={};", path, express, ex));

        }

    }

}
