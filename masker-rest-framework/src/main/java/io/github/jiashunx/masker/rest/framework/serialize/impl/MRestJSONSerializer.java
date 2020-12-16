package io.github.jiashunx.masker.rest.framework.serialize.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jiashunx.masker.rest.framework.exception.MRestSerializeException;
import io.github.jiashunx.masker.rest.framework.serialize.ISerializer;

/**
 * @author jiashunx
 */
public class MRestJSONSerializer implements ISerializer {

    @Override
    public byte[] serialize(Object object) {
        try {
            return new ObjectMapper().writeValueAsBytes(object);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    @Override
    public <T> T deserialize(Class<T> klass, byte[] bytes) {
        try {
            return new ObjectMapper().readValue(bytes, klass);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

}
