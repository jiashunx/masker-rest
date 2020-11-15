package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestResponse {

    private final ChannelHandlerContext channelHandlerContext;
    private final MRestServer restServer;

    public MRestResponse(ChannelHandlerContext ctx, MRestServer restServer) {
        this.channelHandlerContext = Objects.requireNonNull(ctx);
        this.restServer = restServer;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    public void redirect(String targetURL) {
        redirect(channelHandlerContext, targetURL);
    }

    public static void redirect(ChannelHandlerContext ctx, String targetURL) {
        write(ctx, HttpResponseStatus.TEMPORARY_REDIRECT, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
    }

    public void forward(String targetURL, MRestRequest request) {
        request.setUrl(targetURL);
        MRestFilterChain filterChain = getRestServer().getFilterChain(targetURL);
        filterChain.doFilter(request, this);
    }

    public void write(HttpResponseStatus status) {
        write(status, null);
    }

    public void write(HttpResponseStatus status, Map<String, Object> headers) {
        write(status, null, headers);
    }

    public void writeJSON(byte[] bytes) {
        writeJSON(bytes, null);
    }

    public void writeJSON(byte[] bytes, Map<String, Object> headers) {
        write(bytes, MRestHeaderBuilder.Build(headers, Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public void write(byte[] bytes, Map<String, Object> headers) {
        write(HttpResponseStatus.OK, bytes, headers);
    }

    public void write(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        write(channelHandlerContext, status, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status) {
        write(ctx, status, null);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Object> headers) {
        write(ctx, status, null, headers);
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes) {
        writeJSON(ctx, bytes, null);
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, bytes, MRestHeaderBuilder.Build(headers, Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        byte[] _bytes = bytes == null ? new byte[0] : bytes;
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(_bytes));
        HttpHeaders httpHeaders = response.headers();
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(httpHeaders::add);
        }
        httpHeaders.add(Constants.HTTP_HEADER_CONTENT_LENGTH, response.content().readableBytes());
        httpHeaders.add(Constants.HTTP_HEADER_CONNECTION, Constants.CONNECTION_KEEP_ALIVE);
        ctx.write(response);
        ctx.flush();
    }

}
