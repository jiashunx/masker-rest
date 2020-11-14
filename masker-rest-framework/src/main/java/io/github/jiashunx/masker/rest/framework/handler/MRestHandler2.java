package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author jiashunx
 */
public class MRestHandler2<T1 extends MRestRequest, T2 extends MRestResponse> extends MRestHandler {

    private BiConsumer<T1, T2> handler;

    public MRestHandler2(String url, BiConsumer<T1, T2> handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandler2(String url, BiConsumer<T1, T2> handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public BiConsumer<T1, T2> getHandler() {
        return handler;
    }

}
