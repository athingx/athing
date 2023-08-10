package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备操作
 */
public interface ThingOp {

    /**
     * 生成操作令牌
     * @return 操作令牌
     */
    String genToken();

    /**
     * 投递数据
     * @param topic 主题
     * @param data 数据
     * @return 投递操作
     */
    CompletableFuture<Void> post(String topic, OpData data);

    /**
     * 操作绑定
     *
     * @param express 绑定主题表达式
     * @return 函数绑定
     */
    ThingOpBind<byte[]> bind(String express);

}
