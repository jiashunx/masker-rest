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

    public MRestResponse redirect(String targetURL) {
        String contextPath = getRestContext().getContextPath();
        if (!contextPath.equals(Constants.DEFAULT_CONTEXT_PATH)) {
            targetURL = contextPath + targetURL;
        }
        return redirectCrossDomain(targetURL);
    }

    public MRestResponse redirectCrossDomain(String targetURL) {
        // 不需考虑context-path, 直接重定向就完事了.
        return write(HttpResponseStatus.TEMPORARY_REDIRECT, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_LOCATION, targetURL));
    }

    public MRestResponse forward(String targetURL, MRestRequest request) {
        request.setUrl(targetURL);
        MRestFilterChain filterChain = getRestContext().getFilterChain(targetURL);
        filterChain.doFilter(request, this);
        return this;
    }

    public MRestResponse write(HttpResponseStatus status) {
        return write(status, new HashMap<>());
    }

    public MRestResponse write(HttpResponseStatus status, Map<String, Object> headers) {
        return write(status, new MRestHeaders(headers));
    }

    public MRestResponse write(HttpResponseStatus status, MRestHeaders headers) {
        return write(status, null, headers);
    }

    public MRestResponse writeJSON(byte[] bytes) {
        return writeJSON(bytes, new HashMap<>());
    }

    public MRestResponse writeJSON(byte[] bytes, Map<String, Object> headers) {
        return writeJSON(bytes, new MRestHeaders(headers));
    }

    public MRestResponse writeJSON(byte[] bytes, MRestHeaders headers) {
        return write(bytes, new MRestHeaders(headers).add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public MRestResponse writeString(String string) {
        return writeString(string, new HashMap<>());
    }

    public MRestResponse writeString(String string, Map<String, Object> headers) {
        return writeString(string, new MRestHeaders(headers));
    }

    public MRestResponse writeString(String string, MRestHeaders headers) {
        return write(string.getBytes(StandardCharsets.UTF_8), headers);
    }

    public MRestResponse write(Object object) {
        return write(object, new HashMap<>());
    }

    public MRestResponse write(Object object, Map<String, Object> headers) {
        return write(object, new MRestHeaders(headers));
    }

    public MRestResponse write(Object object, MRestHeaders headers) {
        return write(MRestSerializer.jsonSerialize(object), headers);
    }

    public MRestResponse write(byte[] bytes) {
        return write(bytes, new HashMap<>());
    }

    public MRestResponse write(byte[] bytes, Map<String, Object> headers) {
        return write(bytes, new MRestHeaders(headers));
    }

    public MRestResponse write(byte[] bytes, MRestHeaders headers) {
        return write(HttpResponseStatus.OK, bytes, headers);
    }

    public MRestResponse write(HttpResponseStatus status, byte[] bytes) {
        return write(status, bytes, new HashMap<>());
    }

    public MRestResponse write(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        return write(status, bytes, new MRestHeaders(headers));
    }

    public synchronized MRestResponse write(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
        if (isWriteMethodInvoked()) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(status, bytes, headers);
        return this;
    }

    public MRestResponse writeStatusPageAsHtml(HttpResponseStatus status) {
        return writeStatusPage(status, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML));
    }

    public MRestResponse writeStatusPage(HttpResponseStatus status) {
        return writeStatusPage(status, new HashMap<>());
    }

    public MRestResponse writeStatusPage(HttpResponseStatus status, Map<String, Object> headers) {
        return writeStatusPage(status, new MRestHeaders(headers));
    }

    public MRestResponse writeStatusPage(HttpResponseStatus status, MRestHeaders headers) {
        return write(status, MResponseHelper.getStatusPageBytes(status), headers);
    }

    public MRestResponse write(HttpResponseStatus status, Object object) {
        return write(status, object, new HashMap<>());
    }

    public MRestResponse write(HttpResponseStatus status, Object object, Map<String, Object> headers) {
        return write(status, object, new MRestHeaders(headers));
    }

    public MRestResponse write(HttpResponseStatus status, Object object, MRestHeaders headers) {
        return write(status, MRestSerializer.jsonSerialize(object), headers);
    }

    public MRestResponse write(File downloadedFile) {
        return write(downloadedFile, new HashMap<>());
    }

    public MRestResponse write(File downloadedFile, Consumer<File> callback) {
        return write(downloadedFile, new HashMap<>(), callback);
    }

    public MRestResponse write(File downloadedFile, Map<String, Object> headers) {
        return write(downloadedFile, new MRestHeaders(headers));
    }

    public MRestResponse write(File downloadedFile, Map<String, Object> headers, Consumer<File> callback) {
        return write(downloadedFile, new MRestHeaders(headers), callback);
    }

    public MRestResponse write(File downloadedFile, MRestHeaders headers) {
        return write(downloadedFile, headers, null);
    }

    public synchronized MRestResponse write(File downloadedFile, MRestHeaders headers, Consumer<File> callback) {
        if (isWriteMethodInvoked()) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(downloadedFile, headers, callback);
        return this;
    }

    public boolean isWriteMethodInvoked() {
        return flushTask != null;
    }

    public boolean isFlushed() {
        return $flushed;
    }

    public MRestResponse setFlushed(boolean flushed) {
        this.$flushed = flushed;
        return this;
    }

    public MRestResponse setHeader(String key, Object value) {
        this.$headers.add(key, value);
        return this;
    }

    public MRestResponse setHeader(Map<String, Object> headers) {
        this.$headers.add(headers);
        return this;
    }

    public MRestResponse removeHeader(String key) {
        this.$headers.remove(key);
        return this;
    }

    public Object getHeader(String key) {
        return this.$headers.get(key);
    }

    public List<Object> getHeaderAll(String key) {
        return this.$headers.getAll(key);
    }

    public MRestResponse removeCookie(String name) {
        return removeCookie(Constants.DEFAULT_CONTEXT_PATH, name);
    }

    public MRestResponse removeCookie(String path, String name) {
        Cookie cookie = new DefaultCookie(name, "");
        cookie.setPath(path);
        return removeCookie(cookie);
    }

    public MRestResponse removeCookie(Cookie cookie) {
        cookie.setMaxAge(0);
        return setCookie(cookie);
    }

    public MRestResponse setCookie(String name, String value) {
        return setCookie(Constants.DEFAULT_CONTEXT_PATH, name, value);
    }

    public MRestResponse setCookie(String path, String name, String value) {
        return setCookie(path, Long.MIN_VALUE, name, value);
    }

    public MRestResponse setCookie(String path, long maxAge, String name, String value) {
        Cookie cookie = new DefaultCookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return setCookie(cookie);
    }

    /**
     * set cookie.
     * @param cookie io.netty.handler.codec.http.cookie.DefaultCookie.
     */
    public MRestResponse setCookie(Cookie cookie) {
        return setHeader(HttpHeaderNames.SET_COOKIE.toString(), ServerCookieEncoder.STRICT.encode(cookie));
    }

    public synchronized MRestResponse flush() {
        if (isFlushed()) {
            throw new MRestServerException("flush method has already been invoked.");
        }
        if (isWriteMethodInvoked()) {
            flushTask.execute();
        } else {
            flushTask = new FlushTask(HttpResponseStatus.OK, null, null);
            flush();
        }
        return this;
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
