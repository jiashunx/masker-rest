package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class MRestHandlerFunction<T extends MRestRequest, R> extends MRestHandler {

    private Function<T, R> handler;

    public MRestHandlerFunction(String url, Function<T, R> handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerFunction(String url, Function<T, R> handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public Function<T, R> getHandler() {
        return handler;
    }

}
