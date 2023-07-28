package io.github.athingx.athing.thing.api.op;

/**
 * 操作QOS
 */
public enum OpQos {

    /**
     * 最多一次
     */
    AT_MOST_ONCE(0),

    /**
     * 至少一次
     */
    AT_LEAST_ONCE(1);

    private final int value;

    OpQos(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
