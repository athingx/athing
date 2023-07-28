package io.github.athingx.athing.thing.impl.op;

import io.github.athingx.athing.thing.api.op.ThingOpBinder;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 设备操作绑定实现
 */
abstract class ThingOpBinderImpl implements ThingOpBinder {

    private final ReentrantLock lock = new ReentrantLock();
    private long total;
    private long success;
    private long failure;
    private long timeout;
    private long rejected;
    private long canceled;

    /**
     * 更新统计
     *
     * @param cause 失败原因
     */
    void updateStatistics(Throwable cause) {
        lock.lock();
        try {

            total++;

            // success
            if (Objects.isNull(cause)) {
                success++;
            }

            // failure
            else {
                failure++;
            }

            // timeout
            if (cause instanceof TimeoutException) {
                timeout++;
            }

            // cancel
            else if (cause instanceof CancellationException) {
                canceled++;
            }

            // rejected
            else if (cause instanceof RejectedExecutionException) {
                rejected++;
            }

        } finally {
            lock.unlock();
        }

    }

    @Override
    public Statistics statistics() {
        lock.lock();
        try {
            return new Statistics(total, success, failure, timeout, rejected, canceled);
        } finally {
            lock.unlock();
        }
    }

}
