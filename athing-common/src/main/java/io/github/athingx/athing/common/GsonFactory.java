package io.github.athingx.athing.common;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

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
     * 虽然写得复杂了点，但...还是可以用，哈哈哈
     */
    private static final JsonDeserializer<Record> recordJsonDeserializer = new JsonDeserializer<>() {

        private Set<String> parseNames(Class<?> clazz, Parameter parameter) throws NoSuchFieldException {
            final Set<String> names = new LinkedHashSet<>();
            final Field field = clazz.getDeclaredField(parameter.getName());
            if (field.isAnnotationPresent(SerializedName.class)) {
                final var anSerializedName = field.getDeclaredAnnotation(SerializedName.class);
                names.add(anSerializedName.value());
                names.addAll(Arrays.asList(anSerializedName.alternate()));
            } else {
                names.add(parameter.getName());
            }
            return names;
        }

        private JsonElement getJsonElement(JsonObject json, Class<?> clazz, Parameter parameter) throws NoSuchFieldException {
            for (final var name : parseNames(clazz, parameter)) {
                if (json.has(name)) {
                    return json.get(name);
                }
            }
            return null;
        }

        @Override
        public Record deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            final var json = element.getAsJsonObject();
            try {
                if (type instanceof Class<?> clazz) {
                    for (final var constructor : clazz.getDeclaredConstructors()) {
                        final var count = constructor.getParameterCount();
                        final var parameters = constructor.getParameters();
                        final var arguments = new Object[count];
                        for (int index = 0; index < count; index++) {
                            final var parameter = parameters[index];
                            final var parameterElement = getJsonElement(json, clazz, parameter);
                            if (Objects.nonNull(parameterElement)) {
                                arguments[index] = context.deserialize(
                                        getJsonElement(json, clazz, parameter),
                                        parameter.getType()
                                );
                            } else {
                                arguments[index] = null;
                            }
                        }
                        final var access = constructor.canAccess(null);
                        try {
                            constructor.setAccessible(true);
                            return (Record) constructor.newInstance(arguments);
                        } finally {
                            constructor.setAccessible(access);
                        }
                    }
                }
            } catch (Throwable cause) {
                throw new JsonParseException(cause);
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
            .registerTypeHierarchyAdapter(Record.class, recordJsonDeserializer)
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
