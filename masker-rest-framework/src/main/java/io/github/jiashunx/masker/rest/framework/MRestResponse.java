package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerException;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestResponse {

    private final ChannelHandlerContext $channelHandlerContext;
    private final MRestServer restServer;
    private final Map<String, Object> $headers = new HashMap<>();
    private FlushTask flushTask = null;
    private boolean flushed = false;

    public MRestResponse(ChannelHandlerContext ctx, MRestServer restServer) {
        this.$channelHandlerContext = Objects.requireNonNull(ctx);
        this.restServer = restServer;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return $channelHandlerContext;
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    public void redirect(String targetURL) {
        redirect($channelHandlerContext, targetURL);
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

    public synchronized void write(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        if (flushTask != null) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(status, bytes, headers);
    }

    public void setHeader(String key, Object value) {
        this.$headers.put(key, value);
    }

    public void setHeader(Map<String, Object> headers) {
        this.$headers.putAll(headers);
    }

    public synchronized void flush() {
        if (flushed) {
            throw new MRestServerException("flush method has already been invoked.");
        }
        if (flushTask != null) {
            flushTask.execute();
            flushed = true;
        } else {
            flushTask = new FlushTask(HttpResponseStatus.OK, null, null);
            flush();
        }
    }

    private class FlushTask {
        final HttpResponseStatus status;
        final byte[] bytes;
        final Map<String, Object> headers;
        FlushTask(HttpResponseStatus status, byte[] bytes,  Map<String, Object> headers) {
            this.status = Objects.requireNonNull(status);
            this.bytes = bytes;
            this.headers = headers == null ? new HashMap<>() : headers;
        }
        void execute() {
            this.headers.putAll($headers);
            write($channelHandlerContext, status, bytes, this.headers);
        }
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/


    public static void redirect(ChannelHandlerContext ctx, String targetURL) {
        write(ctx, HttpResponseStatus.TEMPORARY_REDIRECT, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
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
