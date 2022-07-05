package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.common.GsonFactory;
import io.github.athingx.athing.thing.ThingPath;
import io.github.athingx.athing.thing.impl.util.CompletableFutureUtils;
import io.github.athingx.athing.thing.impl.util.TokenSequencer;
import io.github.athingx.athing.thing.op.*;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.github.athingx.athing.thing.impl.util.CompletableFutureUtils.tryCatchExecute;
import static io.github.athingx.athing.thing.impl.util.CompletableFutureUtils.whenCompleted;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 阿里云alink协议
 */
class ThingOpLinker {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingPath path;
    private final IMqttAsyncClient client;
    private final ExecutorService executor;
    private final TokenSequencer sequencer = new TokenSequencer();

    ThingOpLinker(ThingPath path, IMqttAsyncClient client, ExecutorService executor) {
        this.path = path;
        this.client = client;
        this.executor = executor;
    }

    /**
     * @see ThingOp#genToken()
     */
    String genToken() {
        return sequencer.next();
    }

    /**
     * @see ThingOp#data(String, OpData)
     */
    CompletableFuture<Void> data(String topic, OpData data) {
        return post(topic, data)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/data/post success; topic={};token={};", path, topic, data.token()),
                        ex -> logger.warn("{}/op/data/post failure; topic={};token={};", path, topic, data.token(), ex)
                ));
    }

    private CompletableFuture<Void> post(String topic, OpData data) {
        final String json = GsonFactory.getGson().toJson(data);
        final MqttMessage message = new MqttMessage();
        message.setQos(1);
        message.setPayload(json.getBytes(UTF_8));
        return tryCatchExecute(future -> client.publish(
                topic,
                message,
                new Object(),
                new MqttFutureCallback<>(future))
        );
    }

    /**
     * @see ThingOp#bind(String)
     */
    OpBind<byte[]> bind(String express) {
        return new ByteOpBindImpl<>(express, client::subscribe, (topic, bytes) -> completedFuture(bytes));
    }

    /**
     * @see ThingOp#group()
     */
    OpGroupBind group() {

        // 订阅项集合
        final List<Subscriber.Item> items = new ArrayList<>();
        return new OpGroupBind() {

            /*
             * 绑定时，构造一个订阅代理，在代理中将所有bind(express)的订阅记录到订阅项集合中，
             * 在bind()的时候批量订阅
             */
            @Override
            public OpBind<byte[]> bind(String express) {
                return new ByteOpBindImpl<>(
                        express,
                        (exp, qos, ctx, callback, listener) -> items.add(new Subscriber.Item(exp, qos, callback, listener)),
                        (topic, bytes) -> completedFuture(bytes)
                );
            }

            /*
             * 提交订阅，在此方法中完成批量订阅
             */
            @Override
            public CompletableFuture<OpBinder> commit() {

                // 检查至少要绑定过一个才能提交
                if (items.isEmpty()) {
                    throw new IllegalStateException("commit failure: at least once bind");
                }

                // 参数准备
                final int size = items.size();
                final String[] expressArray = new String[size];
                final int[] qosArray = new int[size];
                final IMqttActionListener[] callbackArray = new IMqttActionListener[size];
                final IMqttMessageListener[] listenerArray = new IMqttMessageListener[size];

                // 填充参数
                for (int index = 0; index < size; index++) {
                    final Subscriber.Item item = items.get(index);
                    expressArray[index] = item.express();
                    qosArray[index] = item.qos();
                    callbackArray[index] = item.callback();
                    listenerArray[index] = item.listener();
                }

                // MQTT批量订阅
                return CompletableFutureUtils.<OpBinder>tryCatchExecute(future -> client.subscribe(expressArray, qosArray, new Object(),
                                new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken token) {
                                        Stream.of(callbackArray).forEach(callback -> callback.onSuccess(token));
                                        future.complete(() -> CompletableFutureUtils.<Void>tryCatchExecute(unbindF -> client.unsubscribe(
                                                        expressArray,
                                                        new Object(),
                                                        new MqttFutureCallback<>(unbindF)
                                                ))
                                                .whenComplete(whenCompleted(
                                                        v -> logger.debug("{}/op/group/unbind success; express={};", path, String.join(",", expressArray)),
                                                        ex -> logger.warn("{}/op/group/unbind failure; express={};", path, String.join(",", expressArray), ex)
                                                )));
                                    }

                                    @Override
                                    public void onFailure(IMqttToken token, Throwable ex) {
                                        Stream.of(callbackArray).forEach(callback -> callback.onFailure(token, ex));
                                        future.completeExceptionally(ex);
                                    }
                                },
                                listenerArray
                        ))
                        .whenComplete(whenCompleted(
                                v -> logger.debug("{}/op/group/bind success; express={};", path, String.join(",", expressArray)),
                                ex -> logger.warn("{}/op/group/bind failure; express={};", path, String.join(",", expressArray), ex)
                        ));
            }

        };
    }

    /**
     * MQTT回调-Future封装
     *
     * @param <T> 成功返回数据类型
     */
    private static class MqttFutureCallback<T> implements IMqttActionListener {

        private final CompletableFuture<T> future;
        private final Supplier<T> supplier;

        MqttFutureCallback(CompletableFuture<T> future, Supplier<T> supplier) {
            this.future = future;
            this.supplier = supplier;
        }

        MqttFutureCallback(CompletableFuture<T> future) {
            this(future, () -> null);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            future.complete(supplier.get());
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            future.completeExceptionally(exception);
        }

    }

    /**
     * 订阅器
     */
    private interface Subscriber {

        /**
         * 用于代理MQTT客户端实现
         *
         * @see IMqttAsyncClient#subscribe(String[], int[], Object, IMqttActionListener, IMqttMessageListener[])
         */
        void subscribe(String topicFilter, int qos, Object userContext, IMqttActionListener callback, IMqttMessageListener messageListener) throws MqttException;

        /**
         * 订阅项
         *
         * @param express  订阅表达式
         * @param qos      QOS
         * @param callback 订阅回调
         * @param listener 消息监听器
         */
        record Item(String express, int qos, IMqttActionListener callback, IMqttMessageListener listener) {

        }

    }


    /**
     * 操作绑定内部实现；
     * 初始源用MqttMessage中来，所以绑定源头为{@code byte[]}
     *
     * @param <V> 绑定目标类型
     */
    private class ByteOpBindImpl<V> extends OpBindImpl<byte[], V> {

        private final String express;
        private final Subscriber subscriber;

        ByteOpBindImpl(String express, Subscriber subscriber, BiFunction<String, ? super byte[], CompletableFuture<V>> mapper) {
            super(mapper);
            this.express = express;
            this.subscriber = subscriber;
        }

        @Override
        <R> OpBindImpl<byte[], R> newOpBind(BiFunction<String, ? super byte[], CompletableFuture<R>> mapper) {
            return new ByteOpBindImpl<>(express, subscriber, mapper);
        }

        @Override
        public CompletableFuture<OpBinder> bind(BiConsumer<String, ? super V> fn) {
            return CompletableFutureUtils.<OpBinder>tryCatchExecute(bindF -> subscriber.subscribe(express, 1, new Object(),
                            new MqttFutureCallback<>(bindF, () -> () ->
                                    CompletableFutureUtils.<Void>tryCatchExecute(unbindF -> client.unsubscribe(
                                                    express,
                                                    new Object(),
                                                    new MqttFutureCallback<>(unbindF)
                                            ))
                                            .whenComplete(whenCompleted(
                                                    v -> logger.debug("{}/op/bind/unbind success; express={};", path, express),
                                                    ex -> logger.warn("{}/op/bind/unbind failure; express={};", path, express, ex)
                                            ))
                            ),
                            (topic, message) -> executor.execute(() -> mapper()
                                    .apply(topic, message.getPayload())
                                    .thenAccept(data -> fn.accept(topic, data))
                                    .whenComplete(whenCompleted(
                                            (v, ex) -> isNotSkipEx(ex),
                                            v -> logger.debug("{}/op/bind/listener success; topic={};message-id={};", path, topic, message.getId()),
                                            ex -> logger.warn("{}/op/bind/listener failure; topic={};message-id={};", path, topic, message.getId(), ex)
                                    )))
                    ))
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/bind/bind success; express={};", path, express),
                            ex -> logger.warn("{}/op/bind/bind failure; express={};", path, express, ex)
                    ));
        }

        @Override
        public <P extends OpData, R extends OpData> CompletableFuture<OpCaller<P, R>> call(Option opOption, BiFunction<String, ? super V, ? extends R> fn) {
            final Map<String, CompletableFuture<R>> futureMap = new ConcurrentHashMap<>();
            return tryCatchExecute(bindF -> subscriber.subscribe(express, 1, new Object(),
                    new MqttFutureCallback<>(bindF, () -> new OpCaller<>() {

                        @Override
                        public CompletableFuture<Void> unbind() {
                            return CompletableFutureUtils.<Void>tryCatchExecute(unbindF ->
                                            client.unsubscribe(
                                                    express,
                                                    new Object(),
                                                    new MqttFutureCallback<>(unbindF)
                                            ))
                                    .whenComplete(whenCompleted(
                                            v -> logger.debug("{}/op/call/unbind success; express={};", path, express),
                                            ex -> logger.warn("{}/op/call/unbind failure; express={};", path, express, ex)
                                    ));
                        }

                        @Override
                        public CompletableFuture<R> call(String topic, P data) {
                            return tryCatchExecute(future ->
                                    futureMap.put(data.token(), future
                                            .orTimeout(opOption.getTimeoutMs(), MILLISECONDS)
                                            .thenCombine(post(topic, data), (r, unused) -> r)
                                            .whenComplete((r, ex) -> futureMap.remove(data.token(), future))
                                            .whenComplete(whenCompleted(
                                                    v -> logger.debug("{}/op/call/post success; topic={};token={};", path, topic, data.token()),
                                                    ex -> logger.warn("{}/op/call/post failure; topic={};token={};", path, topic, data.token(), ex)
                                            ))
                                    ));
                        }

                    }),
                    (topic, message) -> executor.execute(() -> mapper()
                            .apply(topic, message.getPayload())
                            .thenApply(v -> fn.apply(topic, v))
                            .thenAccept(data -> {
                                final CompletableFuture<R> future = futureMap.remove(data.token());
                                if (null == future) {
                                    logger.warn("{}/op/call/reply received; but none token match, maybe timeout! topic={};message-id={};token={};", path, topic, message.getId(), data.token());
                                } else if (!future.complete(data)) {
                                    logger.warn("{}/op/call/reply received; but assign failure, maybe expired. topic={};message-id={};token={};", path, topic, message.getId(), data.token());
                                }
                            })
                            .whenComplete(whenCompleted(
                                    (v, ex) -> isNotSkipEx(ex),
                                    v -> logger.debug("{}/op/call/reply success; topic={};message-id={};", path, topic, message.getId()),
                                    ex -> logger.warn("{}/op/call/reply failure; topic={};message-id={};", path, topic, message.getId(), ex)
                            )))
            ));
        }

    }

}
