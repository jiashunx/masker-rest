package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class MRestHandlerConsumerReq<T extends MRestRequest> extends MRestHandler {

    private Consumer<T> handler;

    public MRestHandlerConsumerReq(String url, Consumer<T> handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerConsumerReq(String url, Consumer<T> handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public Consumer<T> getHandler() {
        return handler;
    }

}
