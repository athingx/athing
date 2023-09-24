package io.github.athingx.athing.thing.api.op;

/**
 * 编解码器
 *
 * @param <T>  编码前类型
 * @param <R>  解码前类型
 * @param <UT> 编码后类型
 * @param <UR> 解码后类型
 */
public interface Codec<T, R, UT, UR> extends Encoder<UT, T>, Decoder<R, UR> {

    /**
     * 链接编解码器
     *
     * @param codec 编解码器
     * @param <XUT> 编码后类型
     * @param <XUR> 解码后类型
     * @return 链接后的编解码器
     */
    default <XUT, XUR> Codec<T, R, XUT, XUR> chain(Codec<UT, UR, XUT, XUR> codec) {
        return new Codec<>() {
            @Override
            public T encode(XUT xut) {
                return Codec.this.encode(codec.encode(xut));
            }

            @Override
            public XUR decode(String topic, R r) {
                return codec.decode(topic, Codec.this.decode(topic, r));
            }
        };
    }

    /**
     * 无编解码器
     *
     * @param <T> 编码前类型
     * @param <R> 解码前类型
     * @return 无编解码器
     */
    static <T, R> Codec<T, R, T, R> none() {
        return new Codec<>() {
            @Override
            public T encode(T t) {
                return t;
            }

            @Override
            public R decode(String topic, R r) {
                return r;
            }
        };
    }

}
