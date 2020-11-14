package io.github.jiashunx.masker.rest.framework.serialize.impl;

import com.alibaba.fastjson.JSON;
import io.github.jiashunx.masker.rest.framework.serialize.ISerializer;

/**
 * @author jiashunx
 */
public class MRestJSONSerializer implements ISerializer {

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> klass, byte[] bytes) {
        return JSON.parseObject(bytes, klass);
    }

}
