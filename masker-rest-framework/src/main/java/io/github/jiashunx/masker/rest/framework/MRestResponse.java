package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestFlushException;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerException;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.model.MRestHeaders;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.github.jiashunx.masker.rest.framework.util.MResponseHelper;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class MRestResponse {

    private final ChannelHandlerContext $channelHandlerContext;
    private MRestServer restServer;
    private final MRestContext restContext;
    private final MRestHeaders $headers = new MRestHeaders();
    private volatile FlushTask flushTask = null;
    private boolean $flushed = false;

    public MRestResponse(ChannelHandlerContext ctx, MRestContext restContext) {
        this.$channelHandlerContext = Objects.requireNonNull(ctx);
        this.restContext = restContext;
        this.restServer = restContext.getRestServer();
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return $channelHandlerContext;
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    public MRestContext getRestContext() {
        return restContext;
    }

    public void redirect(String targetURL) {
        String contextPath = getRestContext().getContextPath();
        if (!contextPath.equals(Constants.DEFAULT_CONTEXT_PATH)) {
            targetURL = contextPath + targetURL;
        }
        MResponseHelper.redirect($channelHandlerContext, targetURL);
    }

    public void redirectCrossDomain(String targetURL) {
        // 不需考虑context-path, 直接重定向就完事了.
        MResponseHelper.redirect($channelHandlerContext, targetURL);
    }

    public void forward(String targetURL, MRestRequest request) {
        request.setUrl(targetURL);
        MRestFilterChain filterChain = getRestContext().getFilterChain(targetURL);
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

    public void write(Object object) {
        write(object, new HashMap<>());
    }

    public void write(Object object, Map<String, Object> headers) {
        write(object, new MRestHeaders(headers));
    }

    public void write(Object object, MRestHeaders headers) {
        write(MRestSerializer.jsonSerialize(object), headers);
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

    public void writeStatusPageAsHtml(HttpResponseStatus status) {
        writeStatusPage(status, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML));
    }

    public void writeStatusPage(HttpResponseStatus status) {
        writeStatusPage(status, new HashMap<>());
    }

    public void writeStatusPage(HttpResponseStatus status, Map<String, Object> headers) {
        writeStatusPage(status, new MRestHeaders(headers));
    }

    public void writeStatusPage(HttpResponseStatus status, MRestHeaders headers) {
        write(status, MResponseHelper.getStatusPageBytes(status), headers);
    }

    public void write(HttpResponseStatus status, Object object) {
        write(status, object, new HashMap<>());
    }

    public void write(HttpResponseStatus status, Object object, Map<String, Object> headers) {
        write(status, object, new MRestHeaders(headers));
    }

    public void write(HttpResponseStatus status, Object object, MRestHeaders headers) {
        write(status, MRestSerializer.jsonSerialize(object), headers);
    }

    public void write(File downloadedFile) {
        write(downloadedFile, new HashMap<>());
    }

    public void write(File downloadedFile, Consumer<File> callback) {
        write(downloadedFile, new HashMap<>(), callback);
    }

    public void write(File downloadedFile, Map<String, Object> headers) {
        write(downloadedFile, new MRestHeaders(headers));
    }

    public void write(File downloadedFile, Map<String, Object> headers, Consumer<File> callback) {
        write(downloadedFile, new MRestHeaders(headers), callback);
    }

    public void write(File downloadedFile, MRestHeaders headers) {
        write(downloadedFile, headers, null);
    }

    public synchronized void write(File downloadedFile, MRestHeaders headers, Consumer<File> callback) {
        if (isWriteMethodInvoked()) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(downloadedFile, headers, callback);
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

    public void removeCookie(String name) {
        removeCookie(Constants.DEFAULT_CONTEXT_PATH, name);
    }

    public void removeCookie(String path, String name) {
        Cookie cookie = new DefaultCookie(name, "");
        cookie.setPath(path);
        removeCookie(cookie);
    }

    public void removeCookie(Cookie cookie) {
        cookie.setMaxAge(0);
        setCookie(cookie);
    }

    public void setCookie(String name, String value) {
        setCookie(Constants.DEFAULT_CONTEXT_PATH, name, value);
    }

    public void setCookie(String path, String name, String value) {
        setCookie(path, Long.MIN_VALUE, name, value);
    }

    public void setCookie(String path, long maxAge, String name, String value) {
        Cookie cookie = new DefaultCookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        setCookie(cookie);
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
        Consumer<File> downloadCallback;
        FlushTask(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
            this.status = Objects.requireNonNull(status);
            this.bytes = bytes;
            this.headers = headers == null ? new MRestHeaders() : headers;
        }
        FlushTask(File downloadedFile, MRestHeaders headers, Consumer<File> downloadCallback) {
            this.isDownloadFile = true;
            this.downloadedFile = Objects.requireNonNull(downloadedFile);
            this.headers = headers == null ? new MRestHeaders() : headers;
            this.downloadCallback = downloadCallback;
        }
        void execute() {
            try {
                this.headers.addAll($headers);
                if (isDownloadFile) {
                    MResponseHelper.write($channelHandlerContext, downloadedFile, this.headers, downloadCallback);
                } else {
                    MResponseHelper.write($channelHandlerContext, status, bytes, this.headers);
                }
            } catch (Throwable throwable) {
                throw new MRestFlushException(throwable);
            }
        }
    }

}
