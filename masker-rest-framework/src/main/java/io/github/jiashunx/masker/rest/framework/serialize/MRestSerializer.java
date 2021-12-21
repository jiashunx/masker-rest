package io.github.jiashunx.masker.rest.framework.serialize;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jiashunx.masker.rest.framework.exception.MRestSerializeException;
import io.github.jiashunx.masker.rest.framework.serialize.impl.MRestJSONSerializer;
import io.github.jiashunx.masker.rest.framework.global.SharedObjects;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author jiashunx
 */
public class MRestSerializer {

    public static byte[] jsonSerialize(Object object) {
        return new MRestJSONSerializer().serialize(object);
    }

    public static <T> T jsonDeserialize(Class<T> klass, byte[] bytes) {
        return new MRestJSONSerializer().deserialize(klass, bytes);
    }

    public static <T> T jsonToObj(String json, Class<T> klass) {
        try {
            return SharedObjects.getObjectMapperFromThreadLocal().readValue(json, klass);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static <T> T jsonToObj(byte[] bytes, Class<T> klass) {
        try {
            return SharedObjects.getObjectMapperFromThreadLocal().readValue(bytes, klass);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static <T> List<T> jsonToList(String json, Class<T> klass) {
        try {
            return SharedObjects.getObjectMapperFromThreadLocal().readValue(json, new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static <T> List<T> jsonToList(byte[] bytes, Class<T> klass) {
        try {
            return SharedObjects.getObjectMapperFromThreadLocal().readValue(bytes, new TypeReference<List<T>>() {
                @Override
                public Type getType() {
                    return super.getType();
                }
            });
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static String objectToJson(Object object) {
        return objectToJson(object, false);
    }

    public static String objectToJson(Object object, boolean pretty) {
        try {
            ObjectMapper objectMapper = SharedObjects.getObjectMapperFromThreadLocal();
            if (pretty) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return objectMapper.writeValueAsString(object);
            }
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    public static byte[] objectToJsonBytes(Object object) {
        return objectToJsonBytes(object, false);
    }

    public static byte[] objectToJsonBytes(Object object, boolean pretty) {
        try {
            ObjectMapper objectMapper = SharedObjects.getObjectMapperFromThreadLocal();
            if (pretty) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(object);
            } else {
                return objectMapper.writeValueAsBytes(object);
            }
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

}
