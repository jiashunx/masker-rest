package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.*;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.model.ExceptionCallbackVo;
import io.github.jiashunx.masker.rest.framework.model.MRestServerThreadModel;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.SharedObjects;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class MRestServerChannelHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(MRestServerChannelHandler.class);

    private static final Set<String> COMMON_STATIC_RESOURCE_URL = new HashSet<>();
    static {
        COMMON_STATIC_RESOURCE_URL.add("/favicon.ico");
    }

    private final MRestServer restServer;

    public MRestServerChannelHandler(MRestServer restServer) {
        this.restServer = Objects.requireNonNull(restServer);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            MRestRequest restRequest = parseRestRequest((HttpRequest) httpObject);
            if (restRequest == null) {
                MRestResponse.write(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                return;
            }
            MRestContext restContext = restRequest.getRestContext();
            MRestResponse restResponse = new MRestResponse(ctx, restContext);

            // reset thread local
            MRestServerThreadModel serverThreadModel = new MRestServerThreadModel();
            serverThreadModel.setRestRequest(restRequest);
            serverThreadModel.setRestResponse(restResponse);
            serverThreadModel.setRestContext(restContext);
            SharedObjects.resetServerThreadModel(serverThreadModel);

            String originUrl = restRequest.getOriginUrl();
            String requestUrl = restRequest.getUrl();

            Exception exception = null;
            try {
                MRestFilterChain filterChain = null;
                if (isCommonStaticResource(originUrl)) {
                    filterChain = restContext.getCommonStaticResourceFilterChain(originUrl);
                } else {
                    filterChain = restContext.getFilterChain(requestUrl);
                }
                filterChain.doFilter(restRequest, restResponse);
                restResponse.setHeader(Constants.HTTP_HEADER_SERVER_FRAMEWORK_NAME, MRestUtils.getFrameworkName());
                restResponse.setHeader(Constants.HTTP_HEADER_SERVER_FRAMEWORK_VERSION, MRestUtils.getFrameworkVersion());
                if (restResponse.getRestServer().isConnectionKeepAlive()) {
                    restResponse.setHeader(Constants.HTTP_HEADER_CONNECTION, Constants.CONNECTION_KEEP_ALIVE);
                }
                restResponse.flush();
            } catch (Throwable throwable) {
                if (logger.isErrorEnabled()) {
                    logger.error("request handle failed, url: {}", requestUrl, throwable);
                }
                exception = handleException(ctx, restRequest, restResponse, throwable);
            } finally {
                restResponse.setFlushed(true);
                SharedObjects.getServerThreadModel().getRestRequest().release();
                SharedObjects.clearServerThreadModel();
            }
            if (exception != null) {
                throw exception;
            }
        }
    }

    private Exception handleException(ChannelHandlerContext ctx, MRestRequest request
            , MRestResponse response, Throwable cause) {
        Consumer<ExceptionCallbackVo> errHandler = request.getRestContext().getDefaultErrorHandler();
        if (errHandler == null) {
            errHandler = vo -> {
                MRestResponse.write(vo.getChannelHandlerContext(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
            };
        }
        ExceptionCallbackVo callbackVo = new ExceptionCallbackVo();
        callbackVo.setChannelHandlerContext(ctx);
        callbackVo.setRestRequest(request);
        callbackVo.setRestResponse(response);
        callbackVo.setThrowable(cause);
        try {
            errHandler.accept(callbackVo);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("ErrorHandler execute failed.", e);
            }
            return e;
        }
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        super.channelRegistered(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    private boolean isCommonStaticResource(String url) {
        return COMMON_STATIC_RESOURCE_URL.contains(url);
    }

    private MRestRequest parseRestRequest(HttpRequest request) {
        MRestRequest restRequest = null;
        if (request instanceof FullHttpRequest) {
            FullHttpRequest httpRequest = (FullHttpRequest) request;
            restRequest = new MRestRequest();
            restRequest.setHttpRequest(httpRequest);
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.uri(), StandardCharsets.UTF_8, true);
            String path = queryStringDecoder.path();
            if (path.length() > 1 && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            restRequest.setOriginUrl(path);
            // 根据url和已配置的context-path来解析出实际context-path
            String _ctxPath = Constants.DEFAULT_CONTEXT_PATH;
            if (path.length() > 1) {
                String _p = path.substring(1);
                int i = _p.indexOf("/");
                if (i > 0) {
                    _ctxPath = _ctxPath + _p.substring(0, i);
                } else {
                    _ctxPath = path;
                }
            }
            MRestContext restContext = restServer.getContext(_ctxPath);
            if (restContext == null) {
                restContext = restServer.getContext(Constants.DEFAULT_CONTEXT_PATH);
            }
            restRequest.setRestContext(restContext);
            String contextPath = restContext.getContextPath();
            restRequest.setContextPath(contextPath);
            String url = path;
            if (url.equals(contextPath)) {
                url = Constants.ROOT_PATH;
            } else if (!Constants.DEFAULT_CONTEXT_PATH.equals(contextPath) && url.startsWith(contextPath)) {
                url = url.substring(contextPath.length());
            }
            restRequest.setUrl(url);
            restRequest.setUrlQuery(queryStringDecoder.rawQuery());
            Map<String, List<String>> originParameters = queryStringDecoder.parameters();
            restRequest.setOriginParameters(originParameters);
            Map<String, String> parameters = new HashMap<>();
            originParameters.forEach((key, value) -> {
                String mergedVal = "";
                if (value != null && !value.isEmpty()) {
                    mergedVal = value.get(value.size() - 1);
                }
                parameters.put(key, mergedVal);
            });
            restRequest.setParameters(parameters);
            restRequest.setMethod(httpRequest.method());
            restRequest.setHeaders(httpRequest.headers());
            // 处理文件上传特定逻辑.
            if (restRequest.isUploadFile()) {
                MRestFileUploadRequest fileUploadRequest = new MRestFileUploadRequest(restRequest);
                //decode multipart data, request为FullHttpRequest类型
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                while (decoder.hasNext()) {
                    InterfaceHttpData httpData = decoder.next();
                    if (httpData instanceof Attribute) {
                        Attribute attribute = (Attribute) httpData;
                        fileUploadRequest.addAttribute(attribute);
                    } else if (httpData instanceof FileUpload) {
                        FileUpload fileUpload = (FileUpload) httpData;
                        fileUploadRequest.addFileUploadObj(fileUpload);
                    }
                }
                decoder.destroy();
                restRequest = fileUploadRequest;
            } else {
                byte[] bodyBytes = null;
                int byteSize = httpRequest.content().readableBytes();
                if (byteSize >= 0) {
                    bodyBytes = new byte[byteSize];
                    httpRequest.content().readBytes(bodyBytes, 0, byteSize);
                }
                restRequest.setBodyBytes(bodyBytes);
            }
        }
        if (restRequest == null && request != null) {
            if (logger.isWarnEnabled()) {
                logger.warn("parse rest request object failed, uri: {}", request.uri());
            }
        }
        return restRequest;
    }

}
