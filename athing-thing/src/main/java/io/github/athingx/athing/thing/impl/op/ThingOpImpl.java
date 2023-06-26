package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.api.domain.OpReply;
import io.github.athingx.athing.thing.api.op.*;
import io.github.athingx.athing.thing.impl.util.TokenSequencer;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.athingx.athing.thing.api.function.CompletableFutureFn.whenCompleted;
import static io.github.athingx.athing.thing.api.function.CompletableFutureFn.whenExceptionally;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;
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

    @Override
    public String genToken() {
        return sequencer.next();
    }

    @Override
    public <V extends OpData> CompletableFuture<Void> post(OpPost<? super V> opPost, V opData) {
        final var token = opData.token();
        final var topic = opPost.topic(opData);
        return _mqtt_post(topic, opData)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/post success; topic={};token={};", path, topic, token),
                        ex -> logger.warn("{}/op/post failure; topic={};token={};", path, topic, token, ex)
                ));
    }

    private CompletableFuture<Void> _mqtt_post(String topic, OpData opData) {

        // 修复alink协议的BUG：当回传的类型为OpReply时，器中的data如果为null，必须以{}的形式回传
        final OpData _fix_data = opData instanceof OpReply<?> reply && Objects.isNull(reply.data())
                ? new OpReply<>(reply.token(), reply.code(), reply.desc(), new HashMap<>())
                : opData;

        // MQTT: message
        final var json = GsonFactory.getGson().toJson(_fix_data);
        final var message = new MqttMessage();
        message.setQos(1);
        message.setPayload(json.getBytes(UTF_8));

        // post future
        final var postF = new MqttActionFuture<Void>();

        // MQTT: publish
        try {
            client.publish(topic, message, new Object(), postF);
        } catch (MqttException cause) {
            postF.completeExceptionally(cause);
        }

        return postF;
    }

    @Override
    public <V> CompletableFuture<ThingBind> bind(final OpBind<? extends V> opBind,
                                                 final BiConsumer<String, ? super V> consumeFn) {

        final var express = opBind.express();

        // ThingBind: init
        final ThingBind bind = () -> {
            final var unbindF = new MqttActionFuture<Void>();
            try {
                client.unsubscribe(opBind.express(), new Object(), unbindF);
            } catch (MqttException cause) {
                unbindF.completeExceptionally(cause);
            }
            return unbindF
                    .whenComplete(whenCompleted(
                            v -> logger.debug("{}/op/consume/unbind success; express={};", path, express),
                            ex -> logger.warn("{}/op/consume/unbind failure; express={};", path, express, ex)
                    ));

        };

        // MQTT: bind
        return _mqtt_bind(express, opBind::decode, consumeFn)
                .thenApply(unused -> bind)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/consume/bind success; express={};", path, express),
                        ex -> logger.warn("{}/op/consume/bind failure; express={};", path, express, ex)
                ));
    }

    private <V> CompletableFuture<Void> _mqtt_bind(final String express,
                                                   final BiFunction<String, byte[], ? extends V> codec,
                                                   final BiConsumer<String, ? super V> consumeFn) {

        // MQTT: listener
        final IMqttMessageListener listener = (topic, message) ->
                executor.execute(() ->
                        ofNullable(codec.apply(topic, message.getPayload()))
                                .ifPresent(v -> consumeFn.accept(topic, v)));

        // MQTT: subscribe
        final var bindF = new MqttActionFuture<Void>();
        try {
            client.subscribe(express, 1, new Object(), bindF, listener);
        } catch (MqttException cause) {
            bindF.completeExceptionally(cause);
        }

        // 返回绑定
        return bindF;
    }

    @Override
    public <T extends OpData, R extends OpData> CompletableFuture<? extends ThingCall<? super T, ? extends R>> bind(
            final OpPost<? super T> opPost,
            final OpBind<? extends R> opBind
    ) {

        final var express = opBind.express();

        final var tokenFutureMap = new ConcurrentHashMap<String, CompletableFuture<R>>();

        // ThingCall: init
        final var call = new ThingCall<T, R>() {

            @Override
            public CompletableFuture<R> call(ThingCall.Option option, T data) {

                // 请求主题
                final var topic = opPost.topic(data);

                // 调用future
                final var callF = new CompletableFuture<R>()
                        .orTimeout(option.getTimeoutMs(), MILLISECONDS)
                        .whenComplete((v, ex) -> tokenFutureMap.remove(data.token()))
                        .whenComplete(whenCompleted(
                                v -> logger.debug("{}/op/call/post success; topic={};token={};", path, topic, data.token()),
                                ex -> logger.warn("{}/op/call/post failure; topic={};token={};", path, topic, data.token(), ex)
                        ));

                // 存入缓存
                tokenFutureMap.put(data.token(), callF);

                // 发起请求，如果请求失败则让调用Future失败
                _mqtt_post(topic, data)
                        .whenComplete(whenExceptionally(callF::completeExceptionally));

                // 返回调用future
                return callF;
            }

            @Override
            public CompletableFuture<R> call(Option option, Function<String, ? extends T> encoder) {
                return call(option, encoder.apply(genToken()));
            }

            @Override
            public CompletableFuture<Void> unbind() {
                final var unbindF = new MqttActionFuture<Void>();
                try {
                    client.unsubscribe(express, new Object(), unbindF);
                } catch (MqttException cause) {
                    unbindF.completeExceptionally(cause);
                }
                return unbindF
                        .whenComplete(whenCompleted(
                                v -> logger.debug("{}/op/call/unbind success; express={};", path, express),
                                ex -> logger.warn("{}/op/call/unbind failure; express={};", path, express, ex)
                        ));
            }
        };

        // 绑定
        final var bindF = _mqtt_bind(opBind.express(), opBind::decode, (topic, data) -> {
            final var token = data.token();
            final var callF = tokenFutureMap.remove(token);
            if (Objects.isNull(callF)) {
                logger.warn("{}/op/call/bind received; but none token match, maybe timeout! topic={};token={};", path, topic, data.token());
            } else if (!callF.complete(data)) {
                logger.warn("{}/op/call/bind received; but assign failure, maybe expired. topic={};token={};", path, topic, data.token());
            }
        });

        // 返回绑定
        return bindF
                .thenApply(unused -> call)
                .whenComplete(whenCompleted(
                        v -> logger.debug("{}/op/call/bind success; express={};", path, express),
                        ex -> logger.warn("{}/op/call/bind failure; express={};", path, express, ex)
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
