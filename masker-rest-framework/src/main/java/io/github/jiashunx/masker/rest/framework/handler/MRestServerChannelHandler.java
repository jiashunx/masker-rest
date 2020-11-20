package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestServerChannelHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final Logger logger = LoggerFactory.getLogger(MRestServerChannelHandler.class);

    private final MRestServer restServer;

    public MRestServerChannelHandler(MRestServer restServer) {
        this.restServer = Objects.requireNonNull(restServer);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            MRestRequest restRequest = parseRestRequest((HttpRequest) httpObject);
            MRestResponse restResponse = new MRestResponse(ctx, restServer);
            if (restRequest == null) {
                restResponse.write(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                restResponse.flush();
                return;
            }
            String contextPath = restServer.getContextPath();
            String url = restRequest.getUrl();
            if (!url.startsWith(contextPath)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("current server contextPath: {}, can't resolve request url: {}", contextPath, url);
                }
                restResponse.write(HttpResponseStatus.NOT_FOUND);
                restResponse.flush();
                return;
            }
            if (url.equals(contextPath)) {
                url = Constants.ROOT_PATH;
            } else {
                url = url.substring(contextPath.length());
            }
            restRequest.setUrl(url);
            MRestFilterChain filterChain = restServer.getFilterChain(restRequest.getUrl());
            filterChain.doFilter(restRequest, restResponse);
            restResponse.setHeader(Constants.HTTP_HEADER_SERVER_FRAMEWORK_NAME, MRestUtils.getFrameworkName());
            restResponse.setHeader(Constants.HTTP_HEADER_SERVER_FRAMEWORK_VERSION, MRestUtils.getFrameworkVersion());
            if (restResponse.getRestServer().isConnectionKeepAlive()) {
                restResponse.setHeader(Constants.HTTP_HEADER_CONNECTION, Constants.CONNECTION_KEEP_ALIVE);
            }
            restResponse.flush();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (logger.isErrorEnabled()) {
            logger.error("", cause);
        }
        MRestResponse.write(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
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
            restRequest.setUrl(path);
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
            byte[] bodyBytes = null;
            int byteSize = httpRequest.content().readableBytes();
            if (byteSize >= 0) {
                bodyBytes = new byte[byteSize];
                httpRequest.content().readBytes(bodyBytes, 0, byteSize);
            }
            restRequest.setBodyBytes(bodyBytes);
        }
        if (restRequest == null && request != null) {
            if (logger.isWarnEnabled()) {
                logger.warn("parse rest request object failed, uri: {}", request.uri());
            }
        }
        return restRequest;
    }

}
