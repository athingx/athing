package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备呼叫绑定
 *
 * @param <T> 请求数据类型
 * @param <R> 应答数据类型
 */
public interface OpCall<T extends OpData, R extends OpData> extends OpBind {

    /**
     * 呼叫
     *
     * @param topic 请求主题
     * @param data  请求数据
     * @return 应答结果
     */
    CompletableFuture<R> calling(String topic, T data);

}
