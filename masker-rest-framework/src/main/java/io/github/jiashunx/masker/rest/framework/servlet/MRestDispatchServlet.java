package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestHandleException;
import io.github.jiashunx.masker.rest.framework.function.VoidFunc;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.github.jiashunx.masker.rest.framework.util.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.*;

/**
 * @author jiashunx
 */
public class MRestDispatchServlet implements MRestServlet {

    private static byte[] DEFAULT_PAGE_BYTES = null;
    static {
        String template = IOUtils.loadContentFromClasspath("masker-rest/template/index.html", MRestDispatchServlet.class.getClassLoader());
        DEFAULT_PAGE_BYTES = MRestUtils.format(template, "mrf.version", MRestUtils.getFrameworkVersion()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        if (restResponse.isWriteMethodInvoked()) {
            return;
        }
        // dispatch request handler
        String requestURL = restRequest.getUrl();
        MRestContext restContext = restRequest.getRestContext();
        MRestHandler restHandler = restContext.getUrlMappingHandler(requestURL, restRequest.getMethod());
        if (restHandler != null) {
            handleRequest(restRequest, restResponse, restHandler);
            return;
        }
        if (Constants.ROOT_PATH.equals(requestURL) || Constants.INDEX_PATH.equals(requestURL)) {
            if (Constants.ROOT_PATH.equals(requestURL)) {
                // 指定了index url, 服务端进行重定向
                String indexUrl = restContext.getIndexUrl();
                if (StringUtils.isNotBlank(indexUrl)) {
                    restResponse.redirect(indexUrl);
                    return;
                }
                // 静态资源指定了index url
                StaticResource indexResource = restContext.getStaticResourceHolder().getResource(Constants.INDEX_PATH);
                if (indexResource != null) {
                    restResponse.redirect(Constants.INDEX_PATH);
                    return;
                }
            }
            // 输出默认masker-rest主页面
            restResponse.write(DEFAULT_PAGE_BYTES, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML));
            return;
        }
        restResponse.writeStatusPage(HttpResponseStatus.NOT_FOUND);
    }

    private void handleRequest(MRestRequest restRequest, MRestResponse restResponse, MRestHandler restHandler) {
        List<HttpMethod> httpMethods = restHandler.getHttpMethods();
        if (!httpMethods.contains(restRequest.getMethod())) {
            restResponse.write(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        switch (restHandler.getType()) {
            case NoInput_NoRet:
            case InputReq_NoRet:
            case InputReqResp_NoRet:
                handleRequestWithNoRet(restRequest, restResponse, restHandler);
                break;
            case NoInput_Ret:
            case InputReq_Ret:
            case InputReqResp_Ret:
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
                case InputReq_NoRet:
                    ((Consumer) handler).accept(restRequest);
                    break;
                case InputReqResp_NoRet:
                    ((BiConsumer) handler).accept(restRequest, restResponse);
                    break;
                case NoInput_NoRet:
                    ((VoidFunc) handler).doSomething();
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
                case InputReqResp_Ret:
                    retObj = ((BiFunction) handler).apply(restRequest, restResponse);
                    break;
                case InputReq_Ret:
                    retObj = ((Function) handler).apply(restRequest);
                    break;
                case NoInput_Ret:
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
