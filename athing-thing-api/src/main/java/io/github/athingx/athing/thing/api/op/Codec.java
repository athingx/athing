package io.github.athingx.athing.thing.api.op;

import java.nio.charset.Charset;

/**
 * 编解码器
 *
 * @param <T>  编码前类型
 * @param <R>  解码前类型
 * @param <UT> 编码后类型
 * @param <UR> 解码后类型
 */
public interface Codec<T, R, UT, UR> {

    Encoder<UT, T> encoder();

    Decoder<R, UR> decoder();

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
            public Encoder<XUT, T> encoder() {
                return Codec.this.encoder().compose(codec.encoder());
            }

            @Override
            public Decoder<R, XUR> decoder() {
                return Codec.this.decoder().then(codec.decoder());
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
            public Encoder<T, T> encoder() {
                return t -> t;
            }

            @Override
            public Decoder<R, R> decoder() {
                return (topic, r) -> r;
            }

        };
    }

    /**
     * {@code byte[]-json}编解码器
     *
     * @param charset 字符编码
     * @return {@code byte[]-json}编解码器
     */
    static Codec<byte[], byte[], String, String> codecBytesToJson(Charset charset) {
        return new Codec<>() {

            @Override
            public Encoder<String, byte[]> encoder() {
                return Encoder.encodeJsonToBytes(charset);
            }

            @Override
            public Decoder<byte[], String> decoder() {
                return Decoder.decodeBytesToJson(charset);
            }

        };
    }

    /**
     * {@code json-OpCaller}编解码器
     *
     * @param typeForOpRequest 请求类型
     * @param typeForOpReply   应答类型
     * @param <T>              请求类型
     * @param <R>              应答类型
     * @return {@code json-OpCaller}编解码器
     */
    static <T, R> Codec<String, String, OpRequest<T>, OpReply<R>> codecJsonToOpCaller(Class<T> typeForOpRequest, Class<R> typeForOpReply) {
        return new Codec<>() {

            @Override
            public Encoder<OpRequest<T>, String> encoder() {
                return Encoder.encodeOpRequestToJson(typeForOpRequest);
            }

            @Override
            public Decoder<String, OpReply<R>> decoder() {
                return Decoder.decodeJsonToOpReply(typeForOpReply);
            }

        };
    }

    /**
     * {@code json-OpService}编解码器
     *
     * @param typeForOpRequest 请求类型
     * @param typeForOpReply   应答类型
     * @param <T>              请求类型
     * @param <R>              应答类型
     * @return {@code json-OpService}编解码器
     */
    static <T, R> Codec<String, String, OpReply<R>, OpRequest<T>> codecJsonToOpServices(Class<T> typeForOpRequest, Class<R> typeForOpReply) {
        return new Codec<>() {

            @Override
            public Encoder<OpReply<R>, String> encoder() {
                return Encoder.encodeOpReplyToJson(typeForOpReply);
            }

            @Override
            public Decoder<String, OpRequest<T>> decoder() {
                return Decoder.decodeJsonToOpRequest(typeForOpRequest);
            }

        };
    }

}
