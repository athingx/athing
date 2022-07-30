package io.github.athingx.athing.common;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import marcono1234.gson.recordadapter.RecordComponentNamingStrategy;
import marcono1234.gson.recordadapter.RecordTypeAdapterFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/**
 * Gson工厂
 */
public class GsonFactory {

    /**
     * Alink协议：{@link Date}采用{@code long}型表示，取值为时间戳
     */
    private static final TypeAdapter<Date> dateTypeAdapterForAliyun = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            out.value(value.getTime());
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            return new Date(in.nextLong());
        }
    };

    /**
     * Alink协议：{@link Boolean}和{@code boolean}采用{@code int}型表示，取值为0和1
     */
    private static final TypeAdapter<Boolean> booleanTypeAdapterForAliyun = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value ? 1 : 0);
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            return in.nextInt() != 0;
        }
    };

    /**
     * Alink协议：{@link Enum}采用{@code int}型表示，默认采用枚举常量在枚举定义中的顺序(ordinal)
     */
    private static final TypeAdapterFactory enumTypeAdapterFactory = new TypeAdapterFactory() {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final Class<? super T> rawType = type.getRawType();
            if (!Enum.class.isAssignableFrom(rawType)) {
                return null;
            }
            return new TypeAdapter<>() {

                private final Enum<?>[] enumConstants = (Enum<?>[]) (rawType).getEnumConstants();

                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (null == value) {
                        out.nullValue();
                    } else {
                        out.value(((Enum<?>) value).ordinal());
                    }
                }

                @SuppressWarnings("unchecked")
                @Override
                public T read(JsonReader in) throws IOException {
                    final int ordinal = in.nextInt();
                    if (null == enumConstants) {
                        return null;
                    }
                    final Enum<?> target = Arrays.stream(enumConstants)
                            .filter(enumObj -> enumObj.ordinal() == ordinal)
                            .findFirst()
                            .orElse(null);

                    return (T) target;
                }

            };
        }
    };

    /**
     * {@link Void}序列化为null
     */
    private static final TypeAdapter<Void> voidTypeAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Void value) throws IOException {
            out.nullValue();
        }

        @Override
        public Void read(JsonReader in) throws IOException {
            in.skipValue();
            return null;
        }
    };

    /**
     * {@link Record}无法被gson反序列化，核心原因在{@link Field#set(Object, Object)}注释中有解释。
     * <p>
     * 核心功能由<a href="https://github.com/Marcono1234/gson-record-type-adapter-factory">gson-record-type-adapter-factory</a>提供
     * </p>
     */
    private static final TypeAdapterFactory recordTypeAdapterFactory = RecordTypeAdapterFactory.builder()
            .allowMissingComponentValues()
            .allowJsonNullForPrimitiveComponents()
            .withComponentNamingStrategy(RecordComponentNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, dateTypeAdapterForAliyun)
            .registerTypeAdapter(Boolean.class, booleanTypeAdapterForAliyun)
            .registerTypeAdapter(boolean.class, booleanTypeAdapterForAliyun)
            .registerTypeAdapter(Void.class, voidTypeAdapter)
            .registerTypeAdapterFactory(enumTypeAdapterFactory)
            .registerTypeAdapterFactory(recordTypeAdapterFactory)
            .serializeSpecialFloatingPointValues()

            // Alink协议：{@link Long}和{@code long}采用{@code text}型的数字表示
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)

            // .setPrettyPrinting()

            .create();

    /**
     * 获取Gson
     *
     * @return gson
     */
    public static Gson getGson() {
        return gson;
    }

}
