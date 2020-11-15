package io.github.jiashunx.masker.rest.framework.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * rest请求处理配置.
 * @author jiashunx
 */
public class MRestHandlerConfig {

    /**
     * 响应header.
     */
    private Map<String, Object> headers = new HashMap<>();

    public MRestHandlerConfig() {}

    public static MRestHandlerConfig newInstance() {
        return new MRestHandlerConfig();
    }

    public static MRestHandlerConfig newInstance(Map<String, Object> headers) {
        MRestHandlerConfig config = newInstance();
        config.headers = Objects.requireNonNull(headers);
        return config;
    }

    public static MRestHandlerConfig newInstance(Consumer<MRestHandlerConfig> consumer) {
        MRestHandlerConfig config = newInstance();
        consumer.accept(config);
        return config;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public String getHeaderToStr(String key) {
        Object value = getHeader(key);
        return value == null ? null : value.toString();
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

    public MRestHandlerConfig setHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

}
