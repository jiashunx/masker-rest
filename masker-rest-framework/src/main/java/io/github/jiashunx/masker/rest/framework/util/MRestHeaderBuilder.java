package io.github.jiashunx.masker.rest.framework.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiashunx
 */
public class MRestHeaderBuilder {

    private Map<String, Object> headers = new HashMap<>();

    private MRestHeaderBuilder() {}

    private MRestHeaderBuilder(Map<String, Object> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers.putAll(headers);
        }
    }

    public static MRestHeaderBuilder builder() {
        return new MRestHeaderBuilder();
    }

    public static MRestHeaderBuilder builder(Map<String, Object> headers) {
        return new MRestHeaderBuilder(headers);
    }

    public static Map<String, Object> Build() {
        return builder().build();
    }

    public static Map<String, Object> Build(Map<String, Object> headers) {
        return builder(headers).build();
    }

    public static Map<String, Object> Build(String key, Object value) {
        return builder().build(key, value);
    }

    public static Map<String, Object> Build(Map<String, Object> headers, String key, Object value) {
        return builder(headers).build(key, value);
    }

    public Map<String, Object> build() {
        return headers;
    }

    public Map<String, Object> build(String key, Object value) {
        return add(key, value).build();
    }

    public MRestHeaderBuilder add(String key, Object value) {
        headers.put(key, value);
        return this;
    }

}
