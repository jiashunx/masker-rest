package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestHandlerConsumerVoid extends MRestHandler {

    private Runnable handler;

    public MRestHandlerConsumerVoid(String url, Runnable handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerConsumerVoid(String url, Runnable handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public Runnable getHandler() {
        return this.handler;
    }

}
