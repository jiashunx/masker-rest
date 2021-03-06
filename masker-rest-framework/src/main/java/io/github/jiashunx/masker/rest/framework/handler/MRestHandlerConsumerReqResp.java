package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.type.MRestHandlerType;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @author jiashunx
 */
public class MRestHandlerConsumerReqResp<T1 extends MRestRequest, T2 extends MRestResponse> extends MRestHandler {

    private final BiConsumer<T1, T2> handler;

    public MRestHandlerConsumerReqResp(String url, BiConsumer<T1, T2> handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerConsumerReqResp(String url, BiConsumer<T1, T2> handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    public BiConsumer<T1, T2> getHandler() {
        return handler;
    }

    @Override
    public MRestHandlerType getType() {
        return MRestHandlerType.InputReqResp_NoRet;
    }
}
