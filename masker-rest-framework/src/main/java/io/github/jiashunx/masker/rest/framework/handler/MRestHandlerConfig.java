package io.github.jiashunx.masker.rest.framework.handler;

import org.apache.commons.lang.StringUtils;

import java.util.function.Consumer;

/**
 * rest请求处理配置.
 * @author jiashunx
 */
public class MRestHandlerConfig {

    private String requestContentType;
    private String responseContentType;

    public MRestHandlerConfig() {}

    public static MRestHandlerConfig newInstance() {
        return new MRestHandlerConfig();
    }

    public static MRestHandlerConfig newInstance(Consumer<MRestHandlerConfig> consumer) {
        MRestHandlerConfig config = newInstance();
        consumer.accept(config);
        return config;
    }

    public boolean isRequestContentTypeEmpty() {
        return StringUtils.isEmpty(getRequestContentType());
    }

    public boolean isResponseContentTypeEmpty() {
        return StringUtils.isEmpty(getResponseContentType());
    }

    public String getRequestContentType() {
        return requestContentType;
    }

    public void setRequestContentType(String requestContentType) {
        this.requestContentType = requestContentType;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseContentType(String responseContentType) {
        this.responseContentType = responseContentType;
    }

}
