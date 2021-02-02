package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.servlet.AbstractRestServlet;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.HttpMethod;
import io.github.jiashunx.masker.rest.framework.type.MRestHandlerType;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author jiashunx
 */
public class ServletMappingHandler {

    private final String requestUrl;
    private final Method handleMethod;
    private final MRestHandlerType handlerType;
    private final Set<HttpMethod> methods = new HashSet<>();

    public ServletMappingHandler(String requestUrl, Method handleMethod) {
        this.requestUrl = MRestUtils.formatPath(requestUrl);
        this.handleMethod = Objects.requireNonNull(handleMethod);
        this.handlerType = Objects.requireNonNull(getMethodHandlerType(handleMethod));
    }

    public static MRestHandlerType getMethodHandlerType(Method method) {
        Class<?>[] types = method.getParameterTypes();
        if (types.length == 0) {
            return MRestHandlerType.NoInput_NoRet;
        }
        if (types.length == 1 && types[0] == MRestRequest.class) {
            return MRestHandlerType.InputReq_NoRet;
        }
        if (types.length == 2 && types[0] == MRestRequest.class && types[1] == MRestResponse.class) {
            return MRestHandlerType.InputReqResp_NoRet;
        }
        return null;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public Method getHandleMethod() {
        return handleMethod;
    }

    public MRestHandlerType getHandlerType() {
        return handlerType;
    }

    public Set<HttpMethod> getMethods() {
        return methods;
    }
}
