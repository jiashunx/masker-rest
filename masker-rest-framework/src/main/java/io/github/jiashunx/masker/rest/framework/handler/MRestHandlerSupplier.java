package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.Supplier;

public class MRestHandlerSupplier<R> extends MRestHandler {

    private Supplier<R> handler;

    public MRestHandlerSupplier(String url, Supplier<R> handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerSupplier(String url, Supplier<R> handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public Supplier<R> getHandler() {
        return this.handler;
    }

}
