package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.*;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.model.ExceptionCallbackVo;
import io.github.jiashunx.masker.rest.framework.model.MRestServerThreadModel;
import io.github.jiashunx.masker.rest.framework.util.MResponseHelper;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.global.SharedObjects;
import io.github.jiashunx.masker.rest.framework.util.MimetypeUtils;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class MRestServerChannelHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(MRestServerChannelHandler.class);

    private final MRestServer restServer;

    public MRestServerChannelHandler(MRestServer restServer) {
        this.restServer = Objects.requireNonNull(restServer);
    }


    /************************************************** ChannelHandler ********************************************/

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object object) throws Exception {
        if (object instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) object);
            return;
        }
        if (object instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) object);
            return;
        }
        MResponseHelper.write(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // super.exceptionCaught(ctx, cause);
        logger.error("error occurred", cause);
        Channel channel = ctx.channel();
        if (channel.isActive()) {
            ctx.close();
        }
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    /************************************************** ChannelHandler ********************************************/


    /************************************************** WebSocket *************************************************/

    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame object) {
        String channelId = ctx.channel().id().toString();
        MWebsocketRequest websocketRequest = webSocketServerHandshakerMap.get(channelId);
        if (websocketRequest == null) {
            return;
        }
        if (object instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(object.content().retain()));
            return;
        }
        MWebsocketContext websocketContext = websocketRequest.getWebsocketContext();
        MWebsocketResponse websocketResponse = new MWebsocketResponse(ctx, websocketContext);
        if (object instanceof CloseWebSocketFrame) {
            webSocketServerHandshakerMap.remove(channelId);
            MRestUtils.tryCatch(() -> {
                BiConsumer<MWebsocketRequest, MWebsocketResponse> inactiveCallback = websocketRequest.getWebsocketContext().getInactiveCallback();
                if (inactiveCallback != null) {
                    inactiveCallback.accept(websocketRequest, websocketResponse);
                }
            }, throwable -> {
                logger.error("{} inactive callback execute failed", websocketContext.getWebSocketContextDesc(), throwable);
            });
            websocketRequest.getHandshaker().close(ctx.channel(), (CloseWebSocketFrame) object.retain());
            // ctx.channel().close();
            return;
        }
        if (object instanceof TextWebSocketFrame) {
            MWebsocketHandler<TextWebSocketFrame> websocketHandler = websocketContext.getTextFrameHandler();
            if (websocketHandler == null) {
                throw new UnsupportedOperationException(String.format("%s not assign the TextWebSocketFrame handler", websocketContext.getWebSocketContextDesc()));
            }
            websocketHandler.execute((TextWebSocketFrame) object, websocketRequest, websocketResponse);
            return;
        }
        if (object instanceof BinaryWebSocketFrame) {
            MWebsocketHandler<BinaryWebSocketFrame> websocketHandler = websocketContext.getBinaryFrameHandler();
            if (websocketHandler == null) {
                throw new UnsupportedOperationException(String.format("%s not assign the BinaryWebSocketFrame handler", websocketContext.getWebSocketContextDesc()));
            }
            websocketHandler.execute((BinaryWebSocketFrame) object, websocketRequest, websocketResponse);
            return;
        }
        if (object instanceof ContinuationWebSocketFrame) {
            MWebsocketHandler<ContinuationWebSocketFrame> websocketHandler = websocketContext.getContinuationFrameHandler();
            if (websocketHandler == null) {
                throw new UnsupportedOperationException(String.format("%s not assign the ContinuationWebSocketFrame handler", websocketContext.getWebSocketContextDesc()));
            }
            websocketHandler.execute((ContinuationWebSocketFrame) object, websocketRequest, websocketResponse);
        }
    }

    /************************************************** WebSocket *************************************************/

    private final Map<String, MWebsocketRequest> webSocketServerHandshakerMap = new ConcurrentHashMap<>();
    private final Map<String, WebSocketServerHandshakerFactory> handshakerFactoryMap = new HashMap<>();

    public WebSocketServerHandshakerFactory getWebSocketServerHandshakerFactory(String webSocketURL) {
        WebSocketServerHandshakerFactory handshakerFactory = handshakerFactoryMap.get(webSocketURL);
        if (handshakerFactory == null) {
            synchronized (this) {
                handshakerFactory = handshakerFactoryMap.get(webSocketURL);
                if (handshakerFactory == null) {
                    final WebSocketServerHandshakerFactory $handshakerFactory = new WebSocketServerHandshakerFactory(webSocketURL, null, false);
                    handshakerFactoryMap.put(webSocketURL, $handshakerFactory);
                    handshakerFactory = $handshakerFactory;
                }
            }
        }
        return handshakerFactory;
    }

    /************************************************** HTTP  ****************************************************/

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest object) throws Exception {
        MRestRequest restRequest = parseHttpRequest(ctx, object);

        // 处理websocket连接请求.
        if (Constants.UPGRADE_WEBSOCKET.equals(restRequest.getHeader(Constants.HTTP_HEADER_UPGRADE))) {
            Channel channel = ctx.channel();
            String websocketUrl = restRequest.getUrl();
            MWebsocketContext websocketContext = restRequest.getRestContext().getWebsocketContext(websocketUrl);
            // 对于未注册WebsocketContext的websocket请求, 直接响应406
            if (websocketContext == null) {
                HttpResponse res = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.NOT_ACCEPTABLE, channel.alloc().buffer(0));
                res.headers().set(HttpHeaderNames.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
                HttpUtil.setContentLength(res, 0);
                channel.writeAndFlush(res, channel.newPromise());
                return;
            }
            String webSocketURL = String.format("%s://%s:%d%s", restRequest.getProtocolNameLowerCase()
                    , restRequest.getRemoteAddress(), restRequest.getRemotePort(), websocketUrl);
            WebSocketServerHandshakerFactory wsFactory = getWebSocketServerHandshakerFactory(webSocketURL);
            WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(object);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
            } else {
                String channelId = channel.id().toString();
                handshaker.handshake(channel, object);
                MWebsocketRequest websocketRequest = new MWebsocketRequest(restRequest);
                websocketRequest.setWebsocketContext(websocketContext);
                websocketRequest.setHandshaker(handshaker);
                webSocketServerHandshakerMap.put(channelId, websocketRequest);
                MRestUtils.tryCatch(() -> {
                    BiConsumer<ChannelHandlerContext, MWebsocketRequest> activeCallback = websocketRequest.getWebsocketContext().getActiveCallback();
                    if (activeCallback != null) {
                        activeCallback.accept(ctx, websocketRequest);
                    }
                }, throwable -> {
                    logger.error("{} active callback execute failed", websocketContext.getWebSocketContextDesc(), throwable);
                });
            }
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
            MRestFilterChain filterChain = restContext.getFilterChain(requestUrl);
            filterChain.doFilter(restRequest, restResponse);
            restResponse.setHeader(Constants.HTTP_HEADER_SERVER_FRAMEWORK_NAME, MRestUtils.getFrameworkName());
            restResponse.setHeader(Constants.HTTP_HEADER_SERVER_FRAMEWORK_VERSION, MRestUtils.getFrameworkVersion());
            restResponse.setHeader(Constants.HTTP_HEADER_SERVER_STARTUP_TIME, restResponse.getRestServer().getStartupTime());
            restResponse.setHeader(Constants.HTTP_HEADER_SERVER_IDENTIFIER, restResponse.getRestServer().getIdentifier());
            restResponse.setCookie(Constants.HTTP_HEADER_SERVER_IDENTIFIER, restResponse.getRestServer().getIdentifier());
            if (restResponse.getRestServer().isConnectionKeepAlive()) {
                restResponse.setHeader(Constants.HTTP_HEADER_CONNECTION, Constants.CONNECTION_KEEP_ALIVE);
            }
            // Content-Type修正
            String acceptContentType = restRequest.getAcceptFirst();
            List<Object> contentTypes = restResponse.getHeaderAll(Constants.HTTP_HEADER_CONTENT_TYPE);
            if (StringUtils.isNotEmpty(acceptContentType) && (Objects.isNull(contentTypes) || contentTypes.contains(MimetypeUtils.DEFAULT_CONTENT_TYPE_VALUE))) {
                restResponse.setHeader(Constants.HTTP_HEADER_CONTENT_TYPE, acceptContentType);
            }
            restResponse.flush();
        } catch (Throwable throwable) {
            logger.error("{} request handle execute failed, url: {}", restContext.getContextDesc(), requestUrl, throwable);
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

    private MRestRequest parseHttpRequest(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        MRestRequest restRequest = new MRestRequest();
        restRequest.setHttpRequest(httpRequest);
        InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().localAddress();
        restRequest.setRemoteAddress(remoteAddress.getAddress().getHostAddress());
        restRequest.setRemotePort(remoteAddress.getPort());
        InetSocketAddress clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        restRequest.setClientAddress(clientAddress.getAddress().getHostAddress());
        restRequest.setClientPort(clientAddress.getPort());
        restRequest.setProtocolName(httpRequest.protocolVersion().protocolName());
        restRequest.setProtocolVersion(httpRequest.protocolVersion().text());
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.uri(), StandardCharsets.UTF_8, true);
        String originUrl = queryStringDecoder.path();
        if (StringUtils.isEmpty(originUrl)) {
            originUrl = Constants.ROOT_PATH;
        }
        restRequest.setOriginUrl(originUrl);

        // 根据url和已配置的context-path来解析出实际context-path
        String _ctxPath = Constants.DEFAULT_CONTEXT_PATH;
        for (String context: restServer.getContextList()) {
            if (context.equals(Constants.DEFAULT_CONTEXT_PATH)) {
                continue;
            }
            if (originUrl.startsWith(context)) {
                _ctxPath = context;
            }
        }
        MRestContext restContext = restServer.getContext(_ctxPath);
        if (restContext == null) {
            restContext = restServer.context();
        }
        restRequest.setRestContext(restContext);

        // 获取context-path及实际url路径
        String contextPath = restContext.getContextPath();
        restRequest.setContextPath(contextPath);
        String url = originUrl;
        if (!contextPath.equals(Constants.DEFAULT_CONTEXT_PATH)) {
            url = originUrl.substring(contextPath.length());
        }
        // 请求路径为context-path, 形如: /context-path -> 应等价于 -> /context-path/
        if (StringUtils.isEmpty(url)) {
            url = Constants.PATH_SEP;
        }
        restRequest.setUrl(url);
        restRequest.setUrlQuery(queryStringDecoder.rawQuery());
        restRequest.setMethod(httpRequest.method());
        restRequest.setHeaders(httpRequest.headers());
        // 处理文件上传特定逻辑.
        if (restRequest.isUploadFile()) {
            MRestFileUploadRequest fileUploadRequest = new MRestFileUploadRequest(restRequest);
            //decode multipart data, request为FullHttpRequest类型
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(httpRequest);
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
            // HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(httpRequest);
            // List<InterfaceHttpData> httpDataList = decoder.getBodyHttpDatas();
            // for (InterfaceHttpData httpData : httpDataList) {
            //     if (httpData.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            //         Attribute attribute = (Attribute) httpData;
            //         fileUploadRequest.addAttribute(attribute);
            //     }
            //     if (httpData.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
            //         FileUpload fileUpload = (FileUpload) httpData;
            //         fileUploadRequest.addFileUploadObj(fileUpload);
            //     }
            // }
            // decoder.destroy();
            restRequest = fileUploadRequest;
        } else {
            byte[] bodyBytes = null;
            int byteSize = httpRequest.content().readableBytes();
            if (byteSize >= 0) {
                bodyBytes = new byte[byteSize];
                httpRequest.content().readBytes(bodyBytes, 0, byteSize);
            } else {
                bodyBytes = new byte[0];
            }
            restRequest.setBodyBytes(bodyBytes);
        }
        // 参数解析
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
        String contentType = httpRequest.headers().get(Constants.HTTP_HEADER_CONTENT_TYPE);
        if (Constants.CONTENT_TYPE_X_WWW_FORM_URLENCODED.equals(contentType)) {
            String content = new String(restRequest.getBodyBytes(), StandardCharsets.UTF_8);
            if (StringUtils.isNotEmpty(content)) {
                String[] kvArr = new String(restRequest.getBodyBytes(), StandardCharsets.UTF_8).split("&");
                for (String kv: kvArr) {
                    int i = kv.indexOf("=");
                    String k = kv;
                    String v = "";
                    if (i >= 0 && i < kv.length() - 1) {
                        k = kv.substring(0, i);
                        v = kv.substring(i + 1);
                    }
                    parameters.put(k, v);
                }
            }
        }
        restRequest.setParameters(parameters);
        return restRequest;
    }

    private Exception handleException(ChannelHandlerContext ctx, MRestRequest request
            , MRestResponse response, Throwable cause) {
        Consumer<ExceptionCallbackVo> errHandler = request.getRestContext().getDefaultErrorHandler();
        if (errHandler == null) {
            errHandler = vo -> {
                MResponseHelper.writeStatusPage(vo.getChannelHandlerContext(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
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
            logger.error("{} ErrorHandler execute failed", request.getRestContext().getContextDesc(), e);
            return e;
        }
        return null;
    }

    /************************************************** HTTP *****************************************************/


}
