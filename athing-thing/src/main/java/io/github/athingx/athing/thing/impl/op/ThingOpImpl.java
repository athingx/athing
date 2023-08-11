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

import static io.github.athingx.athing.thing.api.util.CompletableFutureUtils.whenCompleted;
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
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/post success, token={};topic={}", path, data.token(), topic),
                        ex -> logger.warn("{}/op/post failure, token={};topic={}", path, data.token(), topic, ex)
                ));
    }

    @Override
    public ThingOpBind<byte[]> bind(String express) {
        return new ThingOpBindImpl<>(express, (topic, data) -> data);
    }


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
        public ThingOpBind<V> matches(OpPredicate<? super V> matcher) {
            return new ThingOpBindImpl<>(express, (topic, data) -> {
                final V value = mapper.apply(topic, data);
                if (!matcher.test(topic, value)) {
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
            return pahoMqttSubscribe(express, 1, (topic, message) -> executor.execute(() -> {

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

                    // 转换结果为OpBinder并返回
                    .thenApply(v -> (ThingOpBinder) () -> pahoMqttUnsubscribe(express).whenComplete(whenCompleted(
                            uv -> logger.debug("{}/op/consumer unbind success, express={}", path, express),
                            uex -> logger.warn("{}/op/consumer unbind failure, express={}", path, express, uex)
                    )))

                    // 绑定成功或失败都要记录日志
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/consumer bind success, express={}", path, express),
                            ex -> logger.warn("{}/op/consumer bind failure, express={}", path, express, ex)
                    ));
        }

        @Override
        public <P extends OpData, R extends OpData> CompletableFuture<ThingOpRouteCaller<P, R>> caller(Option option, OpFunction<? super V, ? extends R> mapper) {
            final var futureMap = new ConcurrentHashMap<String, CompletableFuture<R>>();
            return pahoMqttSubscribe(express, 1, (topic, message) -> executor.execute(() -> {

                try {

                    final var data = ThingOpBindImpl.this.mapper.then(mapper).apply(topic, message.getPayload());
                    final var token = data.token();
                    final var future = futureMap.remove(token);

                    // 如果future为空，说明已经超时了
                    if (Objects.isNull(future)) {
                        logger.warn("{}/op/caller/response token not found, maybe timeout! token={};topic={};", path, token, topic);
                        return;
                    }

                    // 成功收到应答
                    logger.debug("{}/op/caller/response received, token={};topic={};", path, token, topic);

                    // 如果future完成失败，说明已经过期了
                    if (!future.complete(data)) {
                        logger.warn("{}/op/caller/response assert failure, maybe expired! token={};topic={};", path, token, topic);
                    }

                } catch (SkipException cause) {
                    logger.debug("{}/op/caller/response skipped! topic={};", path, topic);
                } catch (Throwable cause) {
                    logger.warn("{}/op/caller/response failure! topic={};", path, topic, cause);
                }

            }))
                    .<ThingOpRouteCaller<P, R>>thenApply(unused -> new ThingOpRouteCaller<>() {
                        @Override
                        public CompletableFuture<R> call(String topic, P data) {

                            // call存根
                            final var future = new CompletableFuture<R>();
                            future.orTimeout(option.getTimeoutMs(), TimeUnit.MILLISECONDS)
                                    .whenComplete((v, ex) -> futureMap.remove(data.token()))
                                    .whenComplete(whenCompleted(
                                            v -> logger.debug("{}/op/caller success, token={};", path, data.token()),
                                            ex -> logger.warn("{}/op/caller failure, token={};", path, data.token(), ex)
                                    ));

                            // 记录存根
                            futureMap.put(data.token(), future);

                            // 发送请求
                            return future.thenCombine(
                                    pahoMqttPublish(topic, 1, JsonHelper.toJson(data).getBytes(UTF_8))
                                            .whenComplete(whenCompleted(
                                                    v -> logger.debug("{}/op/caller/request success, token={};topic={}", path, data.token(), topic),
                                                    ex -> logger.warn("{}/op/caller/request failure, token={};topic={}", path, data.token(), topic, ex)
                                            )),
                                    (r, v) -> r
                            );
                        }

                        @Override
                        public CompletableFuture<Void> unbind() {
                            return pahoMqttUnsubscribe(express)
                                    .whenComplete(whenCompleted(
                                            uv -> logger.debug("{}/op/caller unbind success, express={}", path, express),
                                            uex -> logger.warn("{}/op/caller unbind failure, express={}", path, express, uex)
                                    ));
                        }

                    })

                    // 绑定成功或失败都要记录日志
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/caller bind success, express={}", path, express),
                            ex -> logger.warn("{}/op/caller bind failure, express={}", path, express, ex)
                    ));
        }

    }

}
