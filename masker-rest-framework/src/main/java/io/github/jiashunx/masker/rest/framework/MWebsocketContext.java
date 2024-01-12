package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.function.VoidFunc;
import io.github.jiashunx.masker.rest.framework.handler.MWebsocketHandler;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author jiashunx
 */
public class MWebsocketContext {

    private static final Logger logger = LoggerFactory.getLogger(MWebsocketContext.class);

    private final MRestServer restServer;
    private final MRestContext restContext;
    private final String websocketUrl;

    public MWebsocketContext(MRestServer restServer, MRestContext restContext, String websocketUrl) {
        this.restServer = Objects.requireNonNull(restServer);
        this.restContext = Objects.requireNonNull(restContext);
        this.websocketUrl = MRestUtils.formatWebsocketContextPath(websocketUrl);
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    public MRestContext getRestContext() {
        return restContext;
    }

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public String getWebSocketContextDesc() {
        return String.format("%s WebSocketContext[%s]", getRestContext().getContextDesc(), getWebsocketUrl());
    }

    void init() {
        for (VoidFunc voidFunc: websocketHandlerInitTaskList) {
            voidFunc.doSomething();
        }
    }

    private BiConsumer<ChannelHandlerContext, MWebsocketRequest> activeCallback;
    private BiConsumer<MWebsocketRequest, MWebsocketResponse> inactiveCallback;

    public MWebsocketContext channelActiveCallback(BiConsumer<ChannelHandlerContext, MWebsocketRequest> activeCallback) {
        this.activeCallback = Objects.requireNonNull(activeCallback);
        return this;
    }

    public BiConsumer<ChannelHandlerContext, MWebsocketRequest> getActiveCallback() {
        return activeCallback;
    }

    public MWebsocketContext channelInactiveCallback(BiConsumer<MWebsocketRequest, MWebsocketResponse> inactiveCallback) {
        this.inactiveCallback = Objects.requireNonNull(inactiveCallback);
        return this;
    }

    public BiConsumer<MWebsocketRequest, MWebsocketResponse> getInactiveCallback() {
        return inactiveCallback;
    }

    private final List<VoidFunc> websocketHandlerInitTaskList = new ArrayList<>();
    private volatile MWebsocketHandler<WebSocketFrame> frameHandler;
    private volatile MWebsocketHandler<TextWebSocketFrame> textFrameHandler;
    private volatile MWebsocketHandler<BinaryWebSocketFrame> binaryFrameHandler;
    private volatile MWebsocketHandler<ContinuationWebSocketFrame> continuationFrameHandler;

    public MWebsocketHandler<WebSocketFrame> getFrameHandler() {
        return frameHandler;
    }

    public MWebsocketHandler<TextWebSocketFrame> getTextFrameHandler() {
        return textFrameHandler;
    }

    public MWebsocketHandler<BinaryWebSocketFrame> getBinaryFrameHandler() {
        return binaryFrameHandler;
    }

    public MWebsocketHandler<ContinuationWebSocketFrame> getContinuationFrameHandler() {
        return continuationFrameHandler;
    }

    public synchronized MWebsocketContext bindFrameHandler(MWebsocketHandler<WebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getContinuationFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind ContinuationWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            if (getTextFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind TextWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            if (getBinaryFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind BinaryWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            this.frameHandler = Objects.requireNonNull(websocketHandler);
            if (logger.isInfoEnabled()) {
                logger.info("{} register WebSocketFrame handler", getWebSocketContextDesc());
            }
        });
        return this;
    }

    public synchronized MWebsocketContext bindTextFrameHandler(MWebsocketHandler<TextWebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind WebSocketFrame handler.", getWebSocketContextDesc()));
            }
            if (getContinuationFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind ContinuationWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            this.textFrameHandler = Objects.requireNonNull(websocketHandler);
            if (logger.isInfoEnabled()) {
                logger.info("{} register TextWebSocketFrame handler", getWebSocketContextDesc());
            }
        });
        return this;
    }

    public MWebsocketContext bindBinaryFrameHandler(MWebsocketHandler<BinaryWebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind WebSocketFrame handler.", getWebSocketContextDesc()));
            }
            if (getContinuationFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind ContinuationWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            this.binaryFrameHandler = Objects.requireNonNull(websocketHandler);
            if (logger.isInfoEnabled()) {
                logger.info("{} register BinaryWebSocketFrame handler", getWebSocketContextDesc());
            }
        });
        return this;
    }

    public MWebsocketContext bindContinuationFrame(MWebsocketHandler<ContinuationWebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind WebSocketFrame handler.", getWebSocketContextDesc()));
            }
            if (getTextFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind TextWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            if (getBinaryFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("%s has already bind BinaryWebSocketFrame handler.", getWebSocketContextDesc()));
            }
            this.continuationFrameHandler = Objects.requireNonNull(websocketHandler);
            if (logger.isInfoEnabled()) {
                logger.info("{} register ContinuationWebSocketFrame handler", getWebSocketContextDesc());
            }
        });
        return this;
    }

}
