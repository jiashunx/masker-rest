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

    /**
     * 重定向
     * @param targetURL 重定向目标url
     */
    public void redirect(String targetURL) {
        write(HttpResponseStatus.TEMPORARY_REDIRECT, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
    }

    /**
     * 转发
     * @param targetURL 转发目标url
     * @param request request
     */
    public void forward(String targetURL, MRestRequest request) {
        request.setUrl(targetURL);
        MRestFilterChain filterChain = getRestServer().getFilterChain(targetURL);
        filterChain.doFilter(request, this);
    }

    /**
     * 向channel响应状态.
     * @param status HttpResponseStatus
     */
    public void write(HttpResponseStatus status) {
        write(status, null);
    }

    /**
     * 向channel响应状态.
     * @param status HttpResponseStatus
     * @param headers headers
     */
    public void write(HttpResponseStatus status, Map<String, Object> headers) {
        write(status, null, headers);
    }

    /**
     * 向channel输出json数据.
     * @param bytes bytes
     */
    public void writeJSON(byte[] bytes) {
        writeJSON(bytes, null);
    }

    /**
     * 向channel输出json数据.
     * @param bytes bytes
     * @param headers headers
     */
    public void writeJSON(byte[] bytes, Map<String, Object> headers) {
        write(bytes, MRestHeaderBuilder.Build(headers, Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    /**
     * 向channel输出数据.
     * @param bytes bytes
     * @param headers headers
     */
    public void write(byte[] bytes, Map<String, Object> headers) {
        write(HttpResponseStatus.OK, bytes, headers);
    }

    /**
     * 向channel输出数据.
     * @param status HttpResponseStatus
     * @param bytes bytes
     * @param headers headers
     */
    public void write(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        write(channelHandlerContext, status, bytes, headers);
    }

    /**
     * 重定向
     * @param ctx ChannelHandlerContext
     * @param targetURL 重定向目标url
     */
    public static void redirect(ChannelHandlerContext ctx, String targetURL) {
        write(ctx, HttpResponseStatus.FOUND, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
    }

    /**
     * 向指定channel响应状态.
     * @param ctx ChannelHandlerContext
     * @param status HttpResponseStatus
     */
    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status) {
        write(ctx, status, null);
    }

    /**
     * 向指定channel响应状态.
     * @param ctx ChannelHandlerContext
     * @param status HttpResponseStatus
     * @param headers headers
     */
    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Object> headers) {
        write(ctx, status, null, headers);
    }

    /**
     * 向指定channel输出json数据.
     * @param ctx ChannelHandlerContext
     * @param bytes bytes
     */
    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes) {
        writeJSON(ctx, bytes, null);
    }

    /**
     * 向指定channel输出json数据.
     * @param ctx ChannelHandlerContext
     * @param bytes bytes
     * @param headers headers
     */
    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, bytes, MRestHeaderBuilder.Build(headers, Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    /**
     * 向指定channel输出数据.
     * @param ctx ChannelHandlerContext
     * @param bytes bytes
     * @param headers headers
     */
    public static void write(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    /**
     * 向指定channel输出数据.
     * @param ctx ChannelHandlerContext
     * @param status HttpResponseStatus
     * @param bytes bytes
     * @param headers headers
     */
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
