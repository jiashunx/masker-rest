package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.handler.MWebsocketHandler;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class MWebsocketContext {

    private final MRestServer restServer;
    private final String contextPath;

    public MWebsocketContext(MRestServer restServer, String contextPath) {
        this.restServer = Objects.requireNonNull(restServer);
        this.contextPath = MRestUtils.formatContextPath(contextPath);
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void init() {
        for (Runnable runnable: websocketHandlerInitTaskList) {
            runnable.run();
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

    private final List<Runnable> websocketHandlerInitTaskList = new ArrayList<>();
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
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind ContinuationWebSocketFrame handler.", getContextPath()));
            }
            if (getTextFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind TextWebSocketFrame handler.", getContextPath()));
            }
            if (getBinaryFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind BinaryWebSocketFrame handler.", getContextPath()));
            }
            this.frameHandler = Objects.requireNonNull(websocketHandler);
        });
        return this;
    }

    public synchronized MWebsocketContext bindTextFrameHandler(MWebsocketHandler<TextWebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind WebSocketFrame handler.", getContextPath()));
            }
            if (getContinuationFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind ContinuationWebSocketFrame handler.", getContextPath()));
            }
            this.textFrameHandler = Objects.requireNonNull(websocketHandler);
        });
        return this;
    }

    public MWebsocketContext bindBinaryFrameHandler(MWebsocketHandler<BinaryWebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind WebSocketFrame handler.", getContextPath()));
            }
            if (getContinuationFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind ContinuationWebSocketFrame handler.", getContextPath()));
            }
            this.binaryFrameHandler = Objects.requireNonNull(websocketHandler);
        });
        return this;
    }

    public MWebsocketContext bindContinuationFrame(MWebsocketHandler<ContinuationWebSocketFrame> websocketHandler) {
        getRestServer().checkServerState();
        websocketHandlerInitTaskList.add(() -> {
            if (getFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind WebSocketFrame handler.", getContextPath()));
            }
            if (getTextFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind TextWebSocketFrame handler.", getContextPath()));
            }
            if (getBinaryFrameHandler() != null) {
                throw new IllegalArgumentException(String.format("WebsocketContext[%s] has already bind BinaryWebSocketFrame handler.", getContextPath()));
            }
            this.continuationFrameHandler = Objects.requireNonNull(websocketHandler);
        });
        return this;
    }

}
