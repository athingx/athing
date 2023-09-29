package io.github.athingx.athing.common.gson;

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
import static java.util.Optional.ofNullable;

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
     */
    private static final TypeAdapterFactory recordTypeAdapterFactory = RecordTypeAdapterFactory.builder()
            .allowMissingComponentValues()
            .allowJsonNullForPrimitiveComponents()
            .withComponentNamingStrategy(RecordComponentNamingStrategy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final TypeAdapterFactory opReplyTypeAdapterFactory = new TypeAdapterFactory() {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tToken) {
            if ("io.github.athingx.athing.thing.api.op.OpReply".equals(tToken.getRawType().getTypeName())) {
                final var clazz = (Class<?>) (tToken.getRawType());
                try {
                    final var mToken = clazz.getMethod("token");
                    final var mCode = clazz.getMethod("code");
                    final var mDesc = clazz.getMethod("desc");
                    final var mData = clazz.getMethod("data");
                    final var constructor = clazz.getConstructor(String.class, int.class, String.class, Object.class);

                    return new TypeAdapter<>() {
                        @Override
                        public void write(JsonWriter writer, T reply) throws IOException {
                            if (null == reply) {
                                return;
                            }
                            try {
                                writer.beginObject();
                                writer.name("id");
                                writer.value((String) mToken.invoke(reply));
                                writer.name("code");
                                writer.value((int) mCode.invoke(reply));
                                writer.name("message");
                                writer.value((String) mDesc.invoke(reply));
                                writer.name("data");
                                writer.jsonValue(ofNullable(mData.invoke(reply))
                                        .map(gson::toJson)
                                        .orElse("{}"));
                                writer.endObject();
                            } catch (Throwable ex) {
                                throw new IOException(ex);
                            }
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public T read(JsonReader reader) throws IOException {
                            try {
                                reader.beginObject();
                                String token = null;
                                int code = 0;
                                String desc = null;
                                Object data = null;
                                while (reader.hasNext()) {
                                    switch (reader.nextName()) {
                                        case "id":
                                            token = reader.nextString();
                                            break;
                                        case "code":
                                            code = reader.nextInt();
                                            break;
                                        case "message":
                                            desc = reader.nextString();
                                            break;
                                        case "data":
                                            data = gson.fromJson(reader, Object.class);
                                            break;
                                        default:
                                            reader.skipValue();
                                            break;
                                    }
                                }
                                reader.endObject();
                                return (T) constructor.newInstance(token, code, desc, data);
                            } catch (Throwable ex) {
                                throw new IOException(ex);
                            }
                        }
                    };

                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
            }
            return null;
        }
    };

    private static final TypeAdapterFactory opRequestTypeAdapterFactory = new TypeAdapterFactory() {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> tToken) {
            if ("io.github.athingx.athing.thing.api.op.OpRequest".equals(tToken.getRawType().getTypeName())) {
                try {
                    final var cRequest = (Class<?>) (tToken.getRawType());
                    final var mToken = cRequest.getMethod("token");
                    final var mVersion = cRequest.getMethod("version");
                    final var mMethod = cRequest.getMethod("method");
                    final var mExt = cRequest.getMethod("ext");
                    final var mParams = cRequest.getMethod("params");
                    final var constructorOfRequest = cRequest.getConstructor(
                            String.class,
                            String.class,
                            String.class,
                            Class.forName(cRequest.getModule(), "io.github.athingx.athing.thing.api.op.OpRequest$Ext"),
                            Object.class
                    );

                    return new TypeAdapter<>() {
                        @Override
                        public void write(JsonWriter writer, T request) throws IOException {
                            if (null == request) {
                                return;
                            }
                            try {
                                writer.beginObject();
                                writer.name("id");
                                writer.value((String) mToken.invoke(request));
                                writer.name("version");
                                writer.value((String) mVersion.invoke(request));
                                writer.name("method");
                                writer.value((String) mMethod.invoke(request));
                                writer.name("sys");
                                writer.jsonValue(ofNullable(mExt.invoke(request))
                                        .map(gson::toJson)
                                        .orElse("{}"));
                                writer.name("params");
                                writer.jsonValue(ofNullable(mParams.invoke(request))
                                        .map(gson::toJson)
                                        .orElse("{}"));
                                writer.endObject();
                            } catch (Throwable ex) {
                                throw new IOException(ex);
                            }
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public T read(JsonReader reader) throws IOException {
                            try {
                                reader.beginObject();
                                String token = null;
                                String version = null;
                                String method = null;
                                Object ext = null;
                                Object params = null;
                                while (reader.hasNext()) {
                                    switch (reader.nextName()) {
                                        case "id":
                                            token = reader.nextString();
                                            break;
                                        case "version":
                                            version = reader.nextString();
                                            break;
                                        case "method":
                                            method = reader.nextString();
                                            break;
                                        case "sys":
                                            ext = gson.fromJson(reader, Object.class);
                                            break;
                                        case "params":
                                            params = gson.fromJson(reader, Object.class);
                                            break;
                                        default:
                                            reader.skipValue();
                                            break;
                                    }
                                }
                                reader.endObject();
                                return (T) constructorOfRequest.newInstance(
                                        token,
                                        version,
                                        method,
                                        ext,
                                        params
                                );
                            } catch (Throwable ex) {
                                throw new IOException(ex);
                            }
                        }
                    };

                } catch (Throwable ex) {
                    throw new RuntimeException(ex);
                }
            }
            return null;
        }
    };

    private static final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, dateTypeAdapterForAliyun)
            .registerTypeAdapter(Boolean.class, booleanTypeAdapterForAliyun)
            .registerTypeAdapter(boolean.class, booleanTypeAdapterForAliyun)
            .registerTypeAdapter(Void.class, voidTypeAdapter)
            .registerTypeAdapterFactory(enumTypeAdapterFactory)
            .registerTypeAdapterFactory(recordTypeAdapterFactory)
            .registerTypeAdapterFactory(opReplyTypeAdapterFactory)
            .registerTypeAdapterFactory(opRequestTypeAdapterFactory)
            .serializeSpecialFloatingPointValues()

            // Long/long use text
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
