package io.github.jiashunx.masker.rest.framework.servlet.mapping;

/**
 * @author jiashunx
 */
public enum HttpMethod {

    OPTIONS(io.netty.handler.codec.http.HttpMethod.OPTIONS),
    GET(io.netty.handler.codec.http.HttpMethod.GET),
    HEAD(io.netty.handler.codec.http.HttpMethod.HEAD),
    POST(io.netty.handler.codec.http.HttpMethod.POST),
    PUT(io.netty.handler.codec.http.HttpMethod.PUT),
    PATCH(io.netty.handler.codec.http.HttpMethod.PATCH),
    DELETE(io.netty.handler.codec.http.HttpMethod.DELETE),
    TRACE(io.netty.handler.codec.http.HttpMethod.TRACE),
    CONNECT(io.netty.handler.codec.http.HttpMethod.CONNECT);

    private final io.netty.handler.codec.http.HttpMethod method;

    HttpMethod(io.netty.handler.codec.http.HttpMethod method) {
        this.method = method;
    }

    public io.netty.handler.codec.http.HttpMethod getMethod() {
        return this.method;
    }

}
