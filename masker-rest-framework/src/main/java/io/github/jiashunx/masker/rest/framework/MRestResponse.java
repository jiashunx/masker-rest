package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerException;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.model.MRestHeader;
import io.github.jiashunx.masker.rest.framework.model.MRestHeaders;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestResponse {

    private final ChannelHandlerContext $channelHandlerContext;
    private final MRestServer restServer;
    private final MRestHeaders $headers = new MRestHeaders();
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
        write(status, new HashMap<>());
    }

    public void write(HttpResponseStatus status, Map<String, Object> headers) {
        write(status, new MRestHeaders(headers));
    }

    public void write(HttpResponseStatus status, MRestHeaders headers) {
        write(status, null, headers);
    }

    public void writeJSON(byte[] bytes) {
        writeJSON(bytes, new HashMap<>());
    }

    public void writeJSON(byte[] bytes, Map<String, Object> headers) {
        writeJSON(bytes, new MRestHeaders(headers));
    }

    public void writeJSON(byte[] bytes, MRestHeaders headers) {
        write(bytes, new MRestHeaders(headers).add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public void writeString(String string) {
        writeString(string, new HashMap<>());
    }

    public void writeString(String string, Map<String, Object> headers) {
        writeString(string, new MRestHeaders(headers));
    }

    public void writeString(String string, MRestHeaders headers) {
        write(string.getBytes(StandardCharsets.UTF_8), headers);
    }

    public void write(byte[] bytes, Map<String, Object> headers) {
        write(bytes, new MRestHeaders(headers));
    }

    public void write(byte[] bytes, MRestHeaders headers) {
        write(HttpResponseStatus.OK, bytes, headers);
    }

    public void write(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        write(status, bytes, new MRestHeaders(headers));
    }

    public synchronized void write(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
        if (flushTask != null) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(status, bytes, headers);
    }

    public void setHeader(String key, Object value) {
        this.$headers.add(key, value);
    }

    public void setHeader(Map<String, Object> headers) {
        this.$headers.add(headers);
    }

    public void removeHeader(String key) {
        this.$headers.remove(key);
    }

    public Object getHeader(String key) {
        return this.$headers.get(key);
    }

    public List<Object> getHeaderAll(String key) {
        return this.$headers.getAll(key);
    }

    public void setCookie(String name, String value) {
        setCookie(new DefaultCookie(name, value));
    }

    /**
     * set cookie.
     * @param cookie io.netty.handler.codec.http.cookie.DefaultCookie.
     */
    public void setCookie(Cookie cookie) {
        setHeader(HttpHeaderNames.SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode(cookie));
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
        final MRestHeaders headers;
        FlushTask(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
            this.status = Objects.requireNonNull(status);
            this.bytes = bytes;
            this.headers = headers == null ? new MRestHeaders() : headers;
        }
        void execute() {
            this.headers.addAll($headers);
            write($channelHandlerContext, status, bytes, this.headers);
        }
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/


    public static void redirect(ChannelHandlerContext ctx, String targetURL) {
        write(ctx, HttpResponseStatus.TEMPORARY_REDIRECT, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status) {
        write(ctx, status, new MRestHeaders());
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Object> headers) {
        write(ctx, status, null, headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, MRestHeaders headers) {
        write(ctx, status, null, headers);
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes) {
        writeJSON(ctx, bytes, new HashMap<>());
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        writeJSON(ctx, bytes, new MRestHeaders(headers));
    }

    public static void writeJSON(ChannelHandlerContext ctx, byte[] bytes, MRestHeaders headers) {
        write(ctx, bytes, new MRestHeaders(headers).add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, MRestHeaders headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        write(ctx, status, bytes, new MRestHeaders(headers));
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
        byte[] _bytes = bytes == null ? new byte[0] : bytes;
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(_bytes));
        HttpHeaders httpHeaders = response.headers();
        if (headers != null) {
            List<MRestHeader> headerList = headers.getHeaders();
            headerList.forEach(header -> {
                httpHeaders.add(header.getKey(), header.getValue());
            });
        }
        httpHeaders.add(Constants.HTTP_HEADER_CONTENT_LENGTH, response.content().readableBytes());
        ctx.write(response);
        ctx.flush();
    }

}
