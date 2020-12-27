package io.github.jiashunx.masker.rest.framework;

import io.netty.channel.*;

import java.net.SocketAddress;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MWebsocketResponse implements ChannelOutboundInvoker {

    private final ChannelHandlerContext channelHandlerContext;
    private final MWebsocketContext websocketContext;
    private final MRestServer restServer;

    public MWebsocketResponse(ChannelHandlerContext ctx, MWebsocketContext websocketContext) {
        this.channelHandlerContext = Objects.requireNonNull(ctx);
        this.websocketContext = Objects.requireNonNull(websocketContext);
        this.restServer = websocketContext.getRestServer();
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public String getChannelId() {
        return getChannelHandlerContext().channel().id().toString();
    }

    public MWebsocketContext getWebsocketContext() {
        return websocketContext;
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return getChannelHandlerContext().bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return getChannelHandlerContext().connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return getChannelHandlerContext().connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return getChannelHandlerContext().disconnect();
    }

    @Override
    public ChannelFuture close() {
        return getChannelHandlerContext().close();
    }

    @Override
    public ChannelFuture deregister() {
        return getChannelHandlerContext().deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return getChannelHandlerContext().bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return getChannelHandlerContext().connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return getChannelHandlerContext().connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return getChannelHandlerContext().disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return getChannelHandlerContext().close(promise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return getChannelHandlerContext().deregister(promise);
    }

    @Override
    public ChannelOutboundInvoker read() {
        return getChannelHandlerContext().read();
    }

    @Override
    public ChannelFuture write(Object msg) {
        return getChannelHandlerContext().write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return getChannelHandlerContext().write(msg, promise);
    }

    @Override
    public ChannelOutboundInvoker flush() {
        return getChannelHandlerContext().flush();
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return getChannelHandlerContext().writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return getChannelHandlerContext().writeAndFlush(msg);
    }

    @Override
    public ChannelPromise newPromise() {
        return getChannelHandlerContext().newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return getChannelHandlerContext().newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return getChannelHandlerContext().newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return getChannelHandlerContext().newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise() {
        return getChannelHandlerContext().voidPromise();
    }

}
