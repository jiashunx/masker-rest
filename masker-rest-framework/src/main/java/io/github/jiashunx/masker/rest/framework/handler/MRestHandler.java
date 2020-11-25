package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.type.MRestHandlerType;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public abstract class MRestHandler {

    private String url;
    private List<HttpMethod> httpMethods;
    private final MRestHandlerConfig config;

    public MRestHandler(String url, HttpMethod... methodArr) {
        this(url, null, methodArr);
    }

    public MRestHandler(String url, MRestHandlerConfig config, HttpMethod... methodArr) {
        this.url = Objects.requireNonNull(url);
        this.httpMethods = Arrays.asList(methodArr);
        this.config = config == null ? new MRestHandlerConfig() : config;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<HttpMethod> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<HttpMethod> httpMethods) {
        this.httpMethods = httpMethods;
    }

    public MRestHandlerConfig getConfig() {
        return config;
    }

    public abstract Object getHandler();

    public abstract MRestHandlerType getType();

}
