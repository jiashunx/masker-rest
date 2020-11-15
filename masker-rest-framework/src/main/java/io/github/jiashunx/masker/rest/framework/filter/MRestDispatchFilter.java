package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestHandleException;
import io.github.jiashunx.masker.rest.framework.handler.MRestHandler0;
import io.github.jiashunx.masker.rest.framework.handler.MRestHandler1;
import io.github.jiashunx.masker.rest.framework.handler.MRestHandler2;
import io.github.jiashunx.masker.rest.framework.handler.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author jiashunx
 */
public class MRestDispatchFilter implements MRestFilter {

    @Override
    public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
        // dispatch request handler
        String requestURL = restRequest.getUrl();
        MRestServer restServer = filterChain.getRestServer();
        MRestHandler1<MRestRequest> consumerHandler1 = restServer.getConsumerHandler1(requestURL);
        if (consumerHandler1 != null) {
            handleRequest(restRequest, restResponse, consumerHandler1);
            return;
        }
        MRestHandler2<MRestRequest, MRestResponse> consumerHandler2 = restServer.getConsumerHandler2(requestURL);
        if (consumerHandler2 != null) {
            handleRequest(restRequest, restResponse, consumerHandler2);
            return;
        }
        MRestHandler0<MRestRequest, ?> functionHandler = restServer.getFunctionHandler(requestURL);
        if (functionHandler != null) {
            handleRequest(restRequest, restResponse, functionHandler);
            return;
        }
        // TODO 处理静态资源
        restResponse.write(HttpResponseStatus.NOT_FOUND);
    }

    private void handleRequest(MRestRequest restRequest, MRestResponse restResponse, MRestHandler1<MRestRequest> handler) {
        List<HttpMethod> httpMethods = handler.getHttpMethods();
        if (!httpMethods.contains(restRequest.getMethod())) {
            restResponse.write(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        try {
            handler.getHandler().accept(restRequest);
        } catch (Throwable throwable) {
            throw new MRestHandleException(String.format("handle rest request [%s] failed", restRequest.getUrl()), throwable);
        }
        restResponse.write(HttpResponseStatus.OK);
    }

    private void handleRequest(MRestRequest restRequest, MRestResponse restResponse, MRestHandler2<MRestRequest, MRestResponse> handler) {
        List<HttpMethod> httpMethods = handler.getHttpMethods();
        if (!httpMethods.contains(restRequest.getMethod())) {
            restResponse.write(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        // 交给具体处理逻辑进行请求的响应操作.
        try {
            handler.getHandler().accept(restRequest, restResponse);
        } catch (Throwable throwable) {
            throw new MRestHandleException(String.format("handle rest request [%s] failed", restRequest.getUrl()), throwable);
        }
    }

    private <R> void handleRequest(MRestRequest restRequest, MRestResponse restResponse, MRestHandler0<MRestRequest, R> handler) {
        List<HttpMethod> httpMethods = handler.getHttpMethods();
        if (!httpMethods.contains(restRequest.getMethod())) {
            restResponse.write(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        Object retObj = null;
        try {
            retObj = handler.getHandler().apply(restRequest);
        } catch (Throwable throwable) {
            throw new MRestHandleException(String.format("handle rest request [%s] failed", restRequest.getUrl()), throwable);
        }
        if (retObj == null) {
            restResponse.write(HttpResponseStatus.OK);
            return;
        }
        String contentType = Constants.CONTENT_TYPE_APPLICATION_JSON;
        MRestHandlerConfig config = handler.getConfig();
        if (config != null) {
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
