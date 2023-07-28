package io.github.athingx.athing.thing.api.op;

import java.util.concurrent.CompletableFuture;

/**
 * 设备操作绑定
 */
public interface ThingOpBinder {

    /**
     * 解除绑定
     *
     * @return 接绑结果
     */
    CompletableFuture<Void> unbind();

    /**
     * 获取操作统计
     *
     * @return 操作统计
     */
    Statistics statistics();

    /**
     * 操作统计
     *
     * @param total    总次数
     * @param success  成功次数
     * @param failure  失败次数
     * @param timeout  超时次数
     * @param rejected 被拒绝次数
     * @param canceled 被取消次数
     */
    record Statistics(long total, long success, long failure, long timeout, long rejected, long canceled) {

    }

}
