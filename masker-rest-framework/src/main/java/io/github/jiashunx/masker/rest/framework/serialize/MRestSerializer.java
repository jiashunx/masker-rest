package io.github.jiashunx.masker.rest.framework.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.jiashunx.masker.rest.framework.serialize.impl.MRestJSONSerializer;

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

    public static JSONObject parseToJSONObject(String json) {
        return JSON.parseObject(json);
    }

    public static JSONArray parseToJSONArray(String json) {
        return JSON.parseArray(json);
    }

}
