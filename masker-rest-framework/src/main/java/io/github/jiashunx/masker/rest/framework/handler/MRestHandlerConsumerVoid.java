package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.function.VoidFunc;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.type.MRestHandlerType;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestHandlerConsumerVoid extends MRestHandler {

    private final VoidFunc handler;

    public MRestHandlerConsumerVoid(String url, VoidFunc handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerConsumerVoid(String url, VoidFunc handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public VoidFunc getHandler() {
        return this.handler;
    }

    @Override
    public MRestHandlerType getType() {
        return MRestHandlerType.NoInput_NoRet;
    }
}
