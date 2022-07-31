package io.github.athingx.athing.common.gson.record;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

// Does not match com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper behavior
// but instead always uses runtime type to be more deterministic
class RuntimeTypeTypeAdapter<T> extends TypeAdapter<T> {
    private final Gson gson;
    private final TypeAdapter<T> delegate;

    RuntimeTypeTypeAdapter(Gson gson, TypeAdapter<T> delegate) {
        this.gson = gson;
        this.delegate = delegate;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            // Let compile time type adapter handle it; might write custom value
            delegate.write(out, null);
        } else {
            @SuppressWarnings("unchecked")
            TypeAdapter<T> adapter = (TypeAdapter<T>) gson.getAdapter(TypeToken.get(value.getClass()));
            adapter.write(out, value);
        }
    }

    @Override
    public T read(JsonReader in) throws IOException {
        return delegate.read(in);
    }
}
