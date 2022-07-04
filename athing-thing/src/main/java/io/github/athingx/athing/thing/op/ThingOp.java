package io.github.athingx.athing.thing.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备操作
 */
public interface ThingOp {

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
    CompletableFuture<Void> data(String topic, OpData data);

    /**
     * 操作绑定
     *
     * @param express 绑定主题表达式
     * @return 函数绑定
     */
    OpBind<byte[]> bind(String express);

    /**
     * 操作组绑定
     *
     * @return 操作组绑定
     */
    OpGroupBind group();


}
