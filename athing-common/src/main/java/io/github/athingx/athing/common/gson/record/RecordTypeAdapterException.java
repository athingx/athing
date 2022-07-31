package io.github.athingx.athing.common.gson.record;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serial;

/**
 * Exception thrown when creating a type adapter for a Record class fails.
 *
 * @see RecordTypeAdapterFactory#create(Gson, TypeToken)
 */
public class RecordTypeAdapterException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 0L;

    public RecordTypeAdapterException(String message) {
        super(message);
    }

    public RecordTypeAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
