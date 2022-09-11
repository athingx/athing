package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备操作
 */
public interface ThingOp extends OpBindable {

    /**
     * 生成操作令牌
     *
     * @return 操作令牌
     */
    String genToken();

    /**
     * 投递数据
     *
     * @param topic 主题
     * @param data  数据
     * @return 投递应答
     */
    CompletableFuture<Void> post(String topic, OpData data);

    /**
     * 批量绑定
     *
     * @return 批量绑定操作
     */
    OpBatchBinding binding();

}
