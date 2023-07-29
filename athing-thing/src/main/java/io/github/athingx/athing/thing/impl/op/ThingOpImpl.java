package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.op.*;
import io.github.athingx.athing.thing.api.op.domain.OpData;
import io.github.athingx.athing.thing.api.op.function.OpConsumer;
import io.github.athingx.athing.thing.api.op.function.OpFunction;
import io.github.athingx.athing.thing.api.op.function.OpSupplier;
import io.github.athingx.athing.thing.impl.util.TokenSequencer;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static io.github.athingx.athing.thing.api.util.CompletableFutureUtils.executeFuture;
import static io.github.athingx.athing.thing.api.util.CompletableFutureUtils.whenCompleted;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 设备操作实现
 */
public class ThingOpImpl implements ThingOp {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingPath path;
    private final IMqttAsyncClient client;
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
        this.path = path;
        this.client = client;
        this.executor = executor;
    }

    /**
     * 生成操作令牌
     *
     * @return 操作令牌
     */
    private String genToken() {
        return sequencer.next();
    }

    /**
     * 数据投递者实现
     *
     * @param <V> 投递数据类型
     */
    private class ThingOpPosterImpl<V> extends ThingOpBinderImpl implements ThingOpPoster<V> {

        private final PubPort<V> pub;

        /**
         * 数据投递者实现
         *
         * @param pub 发布端口
         */
        private ThingOpPosterImpl(PubPort<V> pub) {
            this.pub = pub;
        }

        @Override
        public CompletableFuture<Void> unbind() {
            // 数据投递没有实际的绑定行为，所以这里的解绑操作为一个虚假的空操作
            return CompletableFuture.<Void>completedFuture(null)
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/poster/unbind success;", path),
                            ex -> logger.warn("{}/op/poster/unbind failure;", path, ex)
                    ));
        }

        @Override
        public CompletableFuture<V> post(OpSupplier<V> supplier) {
            final var token = genToken();
            final var data = supplier.get(token);
            final var topic = pub.topic(data);
            return _mqtt_publish(topic, pub.getQos().getValue(), pub.encode(token, data))
                    .thenApply(unused -> data)
                    .whenComplete(((unused, cause) -> updateStatistics(cause)))
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/poster/post success; token={};topic={};", path, token, topic),
                            ex -> logger.warn("{}/op/poster/post failure; token={};topic={};", path, token, topic, ex)
                    ));
        }

    }

    @Override
    public <V> CompletableFuture<ThingOpPoster<V>> poster(PubPort<V> pub) {
        // 数据投递没有实际的绑定行为，所以这里的绑定操作为一个虚假的空操作
        return CompletableFuture.<ThingOpPoster<V>>completedFuture(new ThingOpPosterImpl<>(pub))
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/poster/bind success;", path),
                        ex -> logger.warn("{}/op/poster/bind failure;", path, ex)
                ));
    }

    private CompletableFuture<Void> _mqtt_publish(String topic, int qos, byte[] payload) {

        // MQTT: message
        final var message = new MqttMessage();
        message.setQos(qos);
        message.setPayload(payload);

        // MQTT: publish
        return executeFuture(new MqttActionFuture<>(), postF -> client.publish(topic, message, new Object(), postF));

    }

    @Override
    public <V> CompletableFuture<ThingOpBinder> consumer(SubPort<V> sub, OpConsumer<V> consumeFn) {

        // 消费操作绑定
        final var binder = new ThingOpBinderImpl() {

            @Override
            public CompletableFuture<Void> unbind() {
                return _mqtt_unsubscribe(sub.getExpress())
                        .whenComplete(whenCompleted(
                                v -> logger.debug("{}/op/consumer/unbind success; express={};", path, sub.getExpress()),
                                ex -> logger.warn("{}/op/consumer/unbind failure; express={};", path, sub.getExpress(), ex)
                        ));
            }

        };


        // 消费MQTT消息监听器
        final IMqttMessageListener listener = (topic, message) -> executor.execute(() -> {


            // 解码消息
            final V data;
            try {

                // 解码消息数据
                data = sub.decode(topic, message.getPayload());

                // 解码消息数据为空，说明本次处理需要丢弃该消息
                if (isNull(data)) {
                    logger.debug("{}/op/consumer decode none, will be ignored! topic={};", path, topic);
                    return;
                }

            } catch (Throwable cause) {
                binder.updateStatistics(cause);
                logger.warn("{}/op/consumer decode error! topic={};", path, topic, cause);
                return;
            }


            // 消费消息
            try {
                consumeFn.accept(topic, data);
                binder.updateStatistics(null);
                logger.debug("{}/op/consumer success; topic={};", path, topic);
            } catch (Throwable cause) {
                binder.updateStatistics(cause);
                logger.warn("{}/op/consumer failure; topic={};", path, topic, cause);
            }

        });

        // MQTT: bind
        return _mqtt_subscribe(sub.getExpress(), sub.getQos().getValue(), listener)
                .thenApply(unused -> (ThingOpBinder) binder)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/consumer/bind success; express={};", path, sub.getExpress()),
                        ex -> logger.warn("{}/op/consumer/bind failure; express={};", path, sub.getExpress(), ex)
                ));
    }

    private CompletableFuture<Void> _mqtt_unsubscribe(String express) {
        return executeFuture(new MqttActionFuture<>(), unbindF
                -> client.unsubscribe(express, new Object(), unbindF));
    }

    private CompletableFuture<Void> _mqtt_subscribe(String express, int qos, IMqttMessageListener listener) {
        return executeFuture(new MqttActionFuture<>(), bindF
                -> client.subscribe(express, qos, new Object(), bindF, listener));
    }

    @Override
    public <T extends OpData, R> CompletableFuture<ThingOpBinder> services(
            SubPort<T> sub,
            OpFunction<T, PubPort<R>> routingPubFn,
            OpFunction<T, CompletableFuture<R>> serviceFn
    ) {

        // 服务操作绑定
        final var binder = new ThingOpBinderImpl() {

            @Override
            public CompletableFuture<Void> unbind() {
                return _mqtt_unsubscribe(sub.getExpress())
                        .whenComplete(whenCompleted(
                                v -> logger.debug("{}/op/service/unbind success; express={};", path, sub.getExpress()),
                                ex -> logger.warn("{}/op/service/unbind failure; express={};", path, sub.getExpress(), ex)
                        ));
            }

        };

        // 服务MQTT消息监听器
        final IMqttMessageListener listener = (topic, message) -> executor.execute(() -> {

            // 解码消息
            final T request;
            final String token;
            try {

                // 解码服务请求
                request = sub.decode(topic, message.getPayload());

                // 解码消息结果为空，说明本次处理需要丢弃该请求
                if (isNull(request)) {
                    logger.debug("{}/op/service/request decode none, will be ignored! topic={};", path, topic);
                    return;
                }

                // 解码操作令牌
                token = requireNonNull(request.getToken(), "token is missing!");

            } catch (Throwable cause) {
                binder.updateStatistics(cause);
                logger.warn("{}/op/service/request decode error! topic={};", path, topic, cause);
                return;
            }

            // 执行服务
            try {

                // 调用服务
                serviceFn.apply(topic, token, request)

                        // 服务调用成功后，准备服务应答
                        .thenCompose(response -> {

                            // 路由服务应答的发布端口
                            final var pub = requireNonNull(routingPubFn.apply(topic, token, request), "response pub-port routing none!");
                            final var responseTopic = pub.topic(response);
                            logger.debug("{}/op/service/response pub-port routing success! token={};topic={};", path, token, responseTopic);

                            // 服务应答
                            return _mqtt_publish(responseTopic, pub.getQos().getValue(), pub.encode(token, response))
                                    .whenComplete(whenCompleted(
                                            v -> logger.debug("{}/op/service/response success! token={};topic={};", path, token, responseTopic),
                                            ex -> logger.warn("{}/op/service/response failure! token={};topic={};", path, token, responseTopic, ex)
                                    ));

                        })

                        // 完整的请求-应答结束，统计并输出本次服务
                        .whenComplete((v, ex) -> binder.updateStatistics(ex))
                        .whenComplete(whenCompleted(
                                v -> logger.debug("{}/op/service success! token={};", path, token),
                                ex -> logger.warn("{}/op/service failure! token={};", path, token, ex)
                        ));

            }

            // 执行服务过程中发生任何异常，都不会回应服务端
            catch (Throwable cause) {
                binder.updateStatistics(cause);
                logger.warn("{}/op/service/execute occur error! token={};topic={};", path, token, topic, cause);
            }

        });

        // 绑定服务
        return _mqtt_subscribe(sub.getExpress(), sub.getQos().getValue(), listener)
                .thenApply(unused -> (ThingOpBinder) binder)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/service/bind success; express={};", path, sub.getExpress()),
                        ex -> logger.warn("{}/op/service/bind failure; express={};", path, sub.getExpress(), ex)
                ));

    }

    /**
     * 设备调用操作实现
     *
     * @param <T> 请求类型
     * @param <R> 应答类型
     */
    private class ThingOpCallerImpl<T, R> extends ThingOpBinderImpl implements ThingOpCaller<T, R> {

        private final PubPort<T> pub;
        private final SubPort<R> sub;
        private final ConcurrentHashMap<String, CompletableFuture<R>> tokenFutureMap;

        private ThingOpCallerImpl(PubPort<T> pub, SubPort<R> sub, ConcurrentHashMap<String, CompletableFuture<R>> tokenFutureMap) {
            this.pub = pub;
            this.sub = sub;
            this.tokenFutureMap = tokenFutureMap;
        }

        @Override
        public CompletableFuture<Void> unbind() {
            return _mqtt_unsubscribe(sub.getExpress())
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/caller/unbind success; express={};", path, sub.getExpress()),
                            ex -> logger.warn("{}/op/caller/unbind failure; express={};", path, sub.getExpress(), ex)
                    ));
        }

        /**
         * 数据调用
         *
         * @param supplier 获取请求函数
         * @return 应答结果
         */
        @Override
        public CompletableFuture<R> call(OpSupplier<T> supplier) {
            return call(new Option(), supplier);
        }


        @Override
        public CompletableFuture<R> call(Option option, OpSupplier<T> supplier) {

            final var token = genToken();
            final var request = supplier.get(token);
            final var topic = pub.topic(request);
            final var payload = pub.encode(token, request);

            // 生成调用存根并存入缓存
            final var callF = new CompletableFuture<R>();
            tokenFutureMap.put(token, callF);

            return callF
                    .thenCombine(
                            _mqtt_publish(topic, pub.getQos().getValue(), payload)
                                    .whenComplete(whenCompleted(
                                            v -> logger.debug("{}/op/call/request success; token={};topic={};", path, token, topic),
                                            ex -> logger.warn("{}/op/call/request failure; token={};topic={};", path, token, topic, ex)
                                    )),
                            (response, unused) -> response
                    )
                    .orTimeout(option.timeoutMs(), MILLISECONDS)
                    .whenComplete((v, ex) -> tokenFutureMap.remove(token))
                    .whenComplete((v, ex) -> updateStatistics(ex))
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/call/response success; token={};topic={};", path, token, topic),
                            ex -> logger.warn("{}/op/call/response failure; token={};topic={};", path, token, topic, ex)
                    ));
        }

    }

    @Override
    public <T, R extends OpData>
    CompletableFuture<ThingOpCaller<T, R>> caller(PubPort<T> pub, SubPort<R> sub) {

        final var tokenFutureMap = new ConcurrentHashMap<String, CompletableFuture<R>>();
        final var caller = new ThingOpCallerImpl<>(pub, sub, tokenFutureMap);

        final IMqttMessageListener listener = (topic, message) -> executor.execute(() -> {

            // 解码应答
            final R response;
            final String token;
            try {

                // 解码应答
                response = sub.decode(topic, message.getPayload());

                // 如果应答解码为空，说明本次消息应该放弃
                if (isNull(response)) {
                    logger.debug("{}/op/call/response decode none, will be ignored! topic={};", path, topic);
                    return;
                }

                // 拿到操作令牌，用于贯穿上下文
                token = requireNonNull(response.getToken(), "token is missing!");

            } catch (Throwable cause) {
                caller.updateStatistics(cause);
                logger.warn("{}/op/call/response decode error! topic={};", path, topic, cause);
                return;
            }

            // 获取调用存根
            final var callF = tokenFutureMap.remove(token);

            // 如果调用存根为空，说明本次应答已经超时，应该放弃
            if (isNull(callF)) {
                logger.warn("{}/op/call/response assign failure: none token match, maybe timeout! topic={};token={};", path, topic, token);
            }

            // 如果完成调用存根失败，说明本次应答已经过期（被其他结果提前完成），应该放弃
            else if (!callF.complete(response)) {
                logger.warn("{}/op/call/response assign failure: maybe expired. topic={};token={};", path, topic, token);
            }

        });

        return _mqtt_subscribe(sub.getExpress(), sub.getQos().getValue(), listener)
                .thenApply(unused -> (ThingOpCaller<T, R>) caller)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/caller/bind success; express={};", path, sub.getExpress()),
                        ex -> logger.warn("{}/op/caller/bind failure; express={};", path, sub.getExpress(), ex)
                ));
    }


    /**
     * MQTT回调-Future封装
     *
     * @param <T> 成功返回数据类型
     */
    private static class MqttActionFuture<T> extends CompletableFuture<T> implements IMqttActionListener {

        private final T target;

        private MqttActionFuture() {
            this(null);
        }

        private MqttActionFuture(T target) {
            this.target = target;
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            complete(target);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            completeExceptionally(exception);
        }

    }

}
