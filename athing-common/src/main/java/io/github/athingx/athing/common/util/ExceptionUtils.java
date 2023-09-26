package io.github.athingx.athing.common.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * 异常工具类
 */
public class ExceptionUtils {

    public static <X extends Throwable> X getCauseBy(Throwable cause, Class<X> causeType) {
        if (cause == null) {
            return null;
        }
        return causeType.isInstance(cause)
                ? causeType.cast(cause)
                : getCauseBy(cause.getCause(), causeType);
    }


    /**
     * 获取异常的根异常
     *
     * @param cause     异常
     * @param causeType 异常类型
     * @param <X>       异常类型
     * @return 根异常
     */
    public static <X extends Throwable> Optional<X> optionalCauseBy(Throwable cause, Class<X> causeType) {
        return Optional.ofNullable(getCauseBy(cause, causeType));
    }

    /**
     * 判断异常是否由指定类型的异常引起
     *
     * @param cause     异常
     * @param causeType 异常类型
     * @return TRUE | FALSE
     */
    public static boolean isCauseBy(Throwable cause, Class<? extends Throwable> causeType) {
        return optionalCauseBy(cause, causeType).isPresent();
    }

    /**
     * 包装为指定异常，如果当前异常已经是指定异常则返回，否则包装为指定异常后返回
     *
     * @param causeType 指定异常类型
     * @param cause     异常
     * @param supplier  指定异常包装器
     * @param <X>       指定异常类型
     * @return 异常
     */
    public static <X extends Throwable> X wrapBy(Class<X> causeType, Throwable cause, Supplier<X> supplier) {
        return causeType.isInstance(cause)
                ? causeType.cast(cause)
                : supplier.get();
    }

}
