package io.github.jiashunx.masker.rest.framework.handler;

import io.netty.handler.codec.http.HttpMethod;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestHandler {

    private String url;
    private List<HttpMethod> httpMethods;
    private MRestHandlerConfig config;

    public MRestHandler(String url, HttpMethod... methodArr) {
        this(url, null, methodArr);
    }

    public MRestHandler(String url, MRestHandlerConfig config, HttpMethod... methodArr) {
        this.url = Objects.requireNonNull(url);
        this.httpMethods = Arrays.asList(methodArr);
        this.config = Objects.requireNonNull(config);
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

    public void setConfig(MRestHandlerConfig config) {
        this.config = config;
    }

}
