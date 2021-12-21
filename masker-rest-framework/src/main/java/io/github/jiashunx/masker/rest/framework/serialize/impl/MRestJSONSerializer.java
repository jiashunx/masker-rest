package io.github.jiashunx.masker.rest.framework.serialize.impl;

import io.github.jiashunx.masker.rest.framework.exception.MRestSerializeException;
import io.github.jiashunx.masker.rest.framework.serialize.ISerializer;
import io.github.jiashunx.masker.rest.framework.global.SharedObjects;

/**
 * @author jiashunx
 */
public class MRestJSONSerializer implements ISerializer {

    @Override
    public byte[] serialize(Object object) {
        try {
            return SharedObjects.getObjectMapperFromThreadLocal().writeValueAsBytes(object);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

    @Override
    public <T> T deserialize(Class<T> klass, byte[] bytes) {
        try {
            return SharedObjects.getObjectMapperFromThreadLocal().readValue(bytes, klass);
        } catch (Throwable throwable) {
            throw new MRestSerializeException(throwable);
        }
    }

}
