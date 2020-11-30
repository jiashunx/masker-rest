package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestHandleException;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.*;

/**
 * @author jiashunx
 */
public class MRestDispatchFilter implements MRestFilter {

    @Override
    public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
        // dispatch request handler
        String requestURL = restRequest.getUrl();
        MRestServer restServer = filterChain.getRestServer();
        MRestHandler restHandler = restServer.getUrlMappingHandler(requestURL);
        if (restHandler != null) {
            handleRequest(restRequest, restResponse, restHandler);
            return;
        }
        restResponse.write(HttpResponseStatus.NOT_FOUND);
    }

    private void handleRequest(MRestRequest restRequest, MRestResponse restResponse, MRestHandler restHandler) {
        List<HttpMethod> httpMethods = restHandler.getHttpMethods();
        if (!httpMethods.contains(restRequest.getMethod())) {
            restResponse.write(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        switch (restHandler.getType()) {
            case NoRet_Req:
            case NoRet_ReqResp:
            case NoRet_Void:
                handleRequestWithNoRet(restRequest, restResponse, restHandler);
                break;
            case Ret_Void:
            case Ret_ReqResp:
                handleRequestWithRet(restRequest, restResponse, restHandler);
                break;
            default:
                restResponse.write(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                break;
        }
    }

    private void handleRequestWithNoRet(MRestRequest restRequest, MRestResponse restResponse, MRestHandler restHandler) {
        try {
            Object handler = restHandler.getHandler();
            switch (restHandler.getType()) {
                case NoRet_Req:
                    ((Consumer) handler).accept(restRequest);
                    break;
                case NoRet_ReqResp:
                    ((BiConsumer) handler).accept(restRequest, restResponse);
                    break;
                case NoRet_Void:
                    ((Runnable) handler).run();
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (Throwable throwable) {
            throw new MRestHandleException(String.format("handle rest request [%s] failed", restRequest.getUrl()), throwable);
        }
        // 未输出, 输出ok.
        if (!restResponse.isWriteMethodInvoked()) {
            restResponse.write(HttpResponseStatus.OK);
        }
    }

    private void handleRequestWithRet(MRestRequest restRequest, MRestResponse restResponse, MRestHandler restHandler) {
        Object retObj = null;
        try {
            Object handler = restHandler.getHandler();
            switch (restHandler.getType()) {
                case Ret_ReqResp:
                    retObj = ((BiFunction) handler).apply(restRequest, restResponse);
                    break;
                case Ret_Req:
                    retObj = ((Function) handler).apply(restRequest);
                    break;
                case Ret_Void:
                    retObj = ((Supplier) handler).get();
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (Throwable throwable) {
            throw new MRestHandleException(String.format("handle rest request [%s] failed", restRequest.getUrl()), throwable);
        }
        // 已输出, 直接返回.
        if (restResponse.isWriteMethodInvoked()) {
            return;
        }
        if (retObj == null) {
            restResponse.write(HttpResponseStatus.OK);
            return;
        }
        String contentType = Constants.CONTENT_TYPE_APPLICATION_JSON;
        MRestHandlerConfig config = restHandler.getConfig();
        if (config.containsHeader(Constants.HTTP_HEADER_CONTENT_TYPE)) {
            contentType = config.getHeaderToStr(Constants.HTTP_HEADER_CONTENT_TYPE);
        }
        byte[] retBytes = null;
        if (retObj instanceof byte[]) {
            retBytes = (byte[]) retObj;
        } else if (Constants.CONTENT_TYPE_APPLICATION_JSON.equals(contentType)) {
            retBytes = MRestSerializer.jsonSerialize(retObj);
        } else {
            retBytes = retObj.toString().getBytes(StandardCharsets.UTF_8);
        }
        restResponse.write(retBytes, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, contentType));
    }

}
