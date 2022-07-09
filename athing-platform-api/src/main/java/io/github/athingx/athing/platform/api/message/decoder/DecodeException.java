package io.github.athingx.athing.platform.api.message.decoder;

/**
 * 消息解码失败
 */
public class DecodeException extends Exception {

    public DecodeException(String message) {
        super("decode failure: %s".formatted(message));
    }

    public DecodeException(ThingMessageDecoder decoder, Throwable cause) {
        super("decode error at decoder: %s".formatted(decoder.getClass().getSimpleName()), cause);
    }

    public DecodeException(ThingMessageDecoder decoder, String message, Throwable cause) {
        super("decode error: %s at decoder: %s".formatted(message, decoder.getClass().getSimpleName()), cause);
    }

}
