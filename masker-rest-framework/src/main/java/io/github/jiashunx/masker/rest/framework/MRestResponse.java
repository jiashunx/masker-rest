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
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * 响应对象属性信息.
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

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
        setAttr("status", status);
        return this;
    }

    public MRestResponse writeString(String text) {
        return writeString(text, new HashMap<>());
    }

    public MRestResponse writeString(String text, Map<String, Object> headers) {
        return writeString(text, new MRestHeaders(headers));
    }

    public MRestResponse writeString(String text, MRestHeaders headers) {
        return writeString(HttpResponseStatus.OK, text, headers);
    }

    public MRestResponse writeString(HttpResponseStatus status, String text) {
        return writeString(status, text, new HashMap<>());
    }

    public MRestResponse writeString(HttpResponseStatus status, String text, Map<String, Object> headers) {
        return writeString(status, text, new MRestHeaders(headers));
    }

    public MRestResponse writeString(HttpResponseStatus status, String text, MRestHeaders headers) {
        return write(status, text.getBytes(StandardCharsets.UTF_8), headers);
    }

    public MRestResponse writeJSON(byte[] bytes) {
        return writeJSON(bytes, new HashMap<>());
    }

    public MRestResponse writeJSON(byte[] bytes, Map<String, Object> headers) {
        return writeJSON(bytes, new MRestHeaders(headers));
    }

    public MRestResponse writeJSON(byte[] bytes, MRestHeaders headers) {
        return writeJSON(HttpResponseStatus.OK, bytes, headers);
    }

    public MRestResponse writeJSON(HttpResponseStatus status, byte[] bytes) {
        return writeJSON(status, bytes, new HashMap<>());
    }

    public MRestResponse writeJSON(HttpResponseStatus status, byte[] bytes, Map<String, Object> headers) {
        return writeJSON(status, bytes, new MRestHeaders(headers));
    }

    public MRestResponse writeJSON(HttpResponseStatus status, byte[] bytes, MRestHeaders headers) {
        return write(status, bytes, new MRestHeaders(headers).add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON));
    }

    public MRestResponse writeJSON(Object object) {
        return writeJSON(object, new HashMap<>());
    }

    public MRestResponse writeJSON(Object object, Map<String, Object> headers) {
        return writeJSON(object, new MRestHeaders(headers));
    }

    public MRestResponse writeJSON(Object object, MRestHeaders headers) {
        return writeJSON(HttpResponseStatus.OK, object, headers);
    }

    public MRestResponse writeJSON(HttpResponseStatus status, Object object) {
        return writeJSON(status, object, new HashMap<>());
    }

    public MRestResponse writeJSON(HttpResponseStatus status, Object object, Map<String, Object> headers) {
        return writeJSON(status, object, new MRestHeaders(headers));
    }

    public MRestResponse writeJSON(HttpResponseStatus status, Object object, MRestHeaders headers) {
        return writeJSON(status, MRestSerializer.jsonSerialize(object), headers);
    }

    public MRestResponse writeStatusPageAsHtml(HttpResponseStatus status) {
        return writeStatusPageAsHtml(status, new HashMap<>());
    }

    public MRestResponse writeStatusPageAsHtml(HttpResponseStatus status, Map<String, Object> headers) {
        return writeStatusPageAsHtml(status, new MRestHeaders(headers));
    }

    public MRestResponse writeStatusPageAsHtml(HttpResponseStatus status, MRestHeaders headers) {
        return writeStatusPage(status, new MRestHeaders(headers).add(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML));
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

    public MRestResponse writeFile(File downloadedFile) {
        return writeFile(downloadedFile, new HashMap<>());
    }

    public MRestResponse writeFile(File downloadedFile, Consumer<File> callback) {
        return writeFile(downloadedFile, new HashMap<>(), callback);
    }

    public MRestResponse writeFile(File downloadedFile, Map<String, Object> headers) {
        return writeFile(downloadedFile, new MRestHeaders(headers));
    }

    public MRestResponse writeFile(File downloadedFile, Map<String, Object> headers, Consumer<File> callback) {
        return writeFile(downloadedFile, new MRestHeaders(headers), callback);
    }

    public MRestResponse writeFile(File downloadedFile, MRestHeaders headers) {
        return writeFile(downloadedFile, headers, null);
    }

    public MRestResponse writeFile(File downloadedFile, MRestHeaders headers, Consumer<File> callback) {
        return writeFile(HttpResponseStatus.OK, downloadedFile, headers, callback);
    }

    public MRestResponse writeFile(HttpResponseStatus status, File downloadedFile) {
        return writeFile(status, downloadedFile, new HashMap<>());
    }

    public MRestResponse writeFile(HttpResponseStatus status, File downloadedFile, Consumer<File> callback) {
        return writeFile(status, downloadedFile, new HashMap<>(), callback);
    }

    public MRestResponse writeFile(HttpResponseStatus status, File downloadedFile, Map<String, Object> headers) {
        return writeFile(status, downloadedFile, new MRestHeaders(headers));
    }

    public MRestResponse writeFile(HttpResponseStatus status, File downloadedFile, Map<String, Object> headers, Consumer<File> callback) {
        return writeFile(status, downloadedFile, new MRestHeaders(headers), callback);
    }

    public MRestResponse writeFile(HttpResponseStatus status, File downloadedFile, MRestHeaders headers) {
        return writeFile(status, downloadedFile, headers, null);
    }

    public synchronized MRestResponse writeFile(HttpResponseStatus status, File downloadedFile, MRestHeaders headers, Consumer<File> callback) {
        if (isWriteMethodInvoked()) {
            throw new MRestServerException("write method has already been invoked.");
        }
        flushTask = new FlushTask(downloadedFile, headers, callback);
        setAttr("status", status);
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

    @Deprecated
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
                MRestHeaders headers = $headers.copy();
                headers.addAll(this.headers);
                if (isDownloadFile) {
                    MResponseHelper.write($channelHandlerContext, downloadedFile, headers, downloadCallback);
                } else {
                    MResponseHelper.write($channelHandlerContext, status, bytes, headers);
                }
            } catch (Throwable throwable) {
                throw new MRestFlushException(throwable);
            }
        }
    }

    public MRestResponse setAttr(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public Object getAttr(String key) {
        return attributes.get(key);
    }

    public HttpResponseStatus getResponseStatus() {
        HttpResponseStatus status = (HttpResponseStatus) getAttr("status");
        if (status == null) {
            status = HttpResponseStatus.OK;
        }
        return status;
    }

}
