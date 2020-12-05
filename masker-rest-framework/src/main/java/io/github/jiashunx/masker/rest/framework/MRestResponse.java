package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestFileDownloadException;
import io.github.jiashunx.masker.rest.framework.exception.MRestFlushException;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerException;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.model.MRestHeader;
import io.github.jiashunx.masker.rest.framework.model.MRestHeaders;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.SharedObjects;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.io.File;
import java.io.RandomAccessFile;
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
    private volatile FlushTask flushTask = null;
    private boolean $flushed = false;

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
        String contextPath = getRestServer().getContextPath();
        if (!contextPath.equals(Constants.DEFAULT_CONTEXT_PATH)) {
            targetURL = contextPath + targetURL;
        }
        redirect($channelHandlerContext, targetURL);
    }

    public void redirectCrossDomain(String targetURL) {
        // 不需考虑context-path, 直接重定向就完事了.
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

    public void write(byte[] bytes) {
        write(bytes, new HashMap<>());
    }

    public void write(byte[] bytes, Map<String, Object> headers) {
        write(bytes, new MRestHeaders(headers));
    }

    public void write(byte[] bytes, MRestHeaders headers) {
        write(HttpResponseStatus.OK, bytes, headers);
    }

    public void write(HttpResponseStatus status, byte[] bytes) {
        write(status, bytes, new HashMap<>());
    }

    public void write(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        write(status, bytes, new MRestHeaders(headers));
    }

    public synchronized void write(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
        if (isWriteMethodInvoked()) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(status, bytes, headers);
    }

    public void write(File downloadedFile) {
        write(downloadedFile, new HashMap<>());
    }

    public void write(File downloadedFile, Map<String, Object> headers) {
        write(downloadedFile, new MRestHeaders(headers));
    }

    public synchronized void write(File downloadedFile, MRestHeaders headers) {
        if (isWriteMethodInvoked()) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(downloadedFile, headers);
    }

    public boolean isWriteMethodInvoked() {
        return flushTask != null;
    }

    public boolean isFlushed() {
        return $flushed;
    }

    public void setFlushed(boolean flushed) {
        this.$flushed = flushed;
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
        if (isFlushed()) {
            throw new MRestServerException("flush method has already been invoked.");
        }
        if (isWriteMethodInvoked()) {
            flushTask.execute();
        } else {
            flushTask = new FlushTask(HttpResponseStatus.OK, null, null);
            flush();
        }
    }

    private class FlushTask {
        HttpResponseStatus status;
        byte[] bytes;
        MRestHeaders headers;
        File downloadedFile;
        boolean isDownloadFile = false;
        FlushTask(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
            this.status = Objects.requireNonNull(status);
            this.bytes = bytes;
            this.headers = headers == null ? new MRestHeaders() : headers;
        }
        FlushTask(File downloadedFile, MRestHeaders headers) {
            this.isDownloadFile = true;
            this.downloadedFile = Objects.requireNonNull(downloadedFile);
            this.headers = headers == null ? new MRestHeaders() : headers;
        }
        void execute() {
            try {
                this.headers.addAll($headers);
                if (isDownloadFile) {
                    write($channelHandlerContext, downloadedFile, this.headers);
                } else {
                    write($channelHandlerContext, status, bytes, this.headers);
                }
            } catch (Throwable throwable) {
                throw new MRestFlushException(throwable);
            } finally {
                setFlushed(true);
                SharedObjects.getServerThreadModel().getRestRequest().release();
                SharedObjects.clearServerThreadModel();
            }
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

    public static void write(ChannelHandlerContext ctx, byte[] bytes) {
        write(ctx, HttpResponseStatus.OK, bytes, new HashMap<>());
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, Map<String, Object> headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, byte[] bytes, MRestHeaders headers) {
        write(ctx, HttpResponseStatus.OK, bytes, headers);
    }

    public static void write(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] bytes) {
        write(ctx, status, bytes, new HashMap<>());
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

    public static void write(ChannelHandlerContext ctx, File downloadedFile) {
        write(ctx, downloadedFile, new HashMap<>());
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, Map<String, Object> headers) {
        write(ctx, downloadedFile, new MRestHeaders(headers));
    }

    public static void write(ChannelHandlerContext ctx, File downloadedFile, MRestHeaders headers) {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(downloadedFile, "r");
            long fileLength = randomAccessFile.length();
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            HttpHeaders httpHeaders = response.headers();
            if (headers != null) {
                List<MRestHeader> headerList = headers.getHeaders();
                headerList.forEach(header -> {
                    httpHeaders.add(header.getKey(), header.getValue());
                });
            }
            httpHeaders.add(Constants.HTTP_HEADER_CONTENT_LENGTH, fileLength);
            httpHeaders.add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_OCTETSTREAM);
            httpHeaders.add(Constants.HTTP_HEADER_CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", downloadedFile.getName()));
            ctx.write(response);
            ChannelFuture sendFileFuture = ctx.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, fileLength), ctx.newProgressivePromise());
            sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
                @Override
                public void operationProgressed(ChannelProgressiveFuture channelProgressiveFuture, long l, long l1) throws Exception {

                }
                @Override
                public void operationComplete(ChannelProgressiveFuture channelProgressiveFuture) throws Exception {
                    randomAccessFile.close();
                }
            });
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (Throwable throwable) {
            throw new MRestFileDownloadException(throwable);
        }
    }

}
