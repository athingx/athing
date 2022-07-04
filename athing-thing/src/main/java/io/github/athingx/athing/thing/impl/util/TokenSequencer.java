package io.github.athingx.athing.thing.impl.util;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.currentTimeMillis;

/**
 * 令牌序列
 * <p>alink协议要求：设备操作令牌必须在{@code 0~4294967295}之间，为32位无符号整型，并且在当前设备中具有唯一性。</p>
 * <p>但协议并未说明唯一性的范围，这里做一个假设：令牌在设备通讯有效窗口周期（7天）内不出现重复令牌号。令牌序列将会按照这个假设完成设计。</p>
 * <p>
 * 7天毫秒数为：{@code 7*24*3600*1000 = 604800000}为9位数，满足令牌最大值的限定设计
 * </p>
 */
public class TokenSequencer {

    // 唯一约束屏障，因为令牌在生成过程为单调递增，所以只用通过判断本次生成和上次是否相同即可完成唯一约束
    private final AtomicInteger uniqueRef = new AtomicInteger(0);

    /**
     * 取时间戳（毫秒）后9位数，考虑到部分机器时钟可能被重置，不足位前补0
     *
     * @return 种子（9位数字的字符串）
     */
    private static int seed() {
        final String seed = "%09d".formatted(currentTimeMillis());
        return Integer.valueOf(seed.substring(seed.length() - 9), 10);
    }

    /**
     * 生成序列
     *
     * @return 令牌序列
     */
    public String next() {
        while (true) {
            final int seed = seed();
            final int last = uniqueRef.get();
            if (seed == last || !uniqueRef.compareAndSet(last, seed)) {
                Thread.yield();
                continue;
            }
            return String.valueOf(seed);
        }
    }

}
