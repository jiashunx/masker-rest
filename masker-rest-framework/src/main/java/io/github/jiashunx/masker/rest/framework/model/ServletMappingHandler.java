package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.servlet.mapping.HttpMethod;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class ServletMappingHandler {

    private final String requestUrl;
    private final Method handleMethod;
    private final List<HttpMethod> methodList = new ArrayList<>();

    public ServletMappingHandler(String requestUrl, Method handleMethod) {
        this.requestUrl = MRestUtils.formatPath(requestUrl);
        this.handleMethod = Objects.requireNonNull(handleMethod);
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public Method getHandleMethod() {
        return handleMethod;
    }

    public List<HttpMethod> getMethodList() {
        return methodList;
    }
}
