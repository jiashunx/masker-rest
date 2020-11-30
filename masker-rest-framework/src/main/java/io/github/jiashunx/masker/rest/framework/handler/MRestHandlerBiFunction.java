package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.type.MRestHandlerType;
import io.netty.handler.codec.http.HttpMethod;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author jiashunx
 */
public class MRestHandlerBiFunction<T extends MRestRequest, U extends MRestResponse, R> extends MRestHandler {

    private final BiFunction<T, U, R> handler;

    public MRestHandlerBiFunction(String url, BiFunction<T, U, R> handler, HttpMethod... methodArr) {
        this(url, handler, MRestHandlerConfig.newInstance(), methodArr);
    }

    public MRestHandlerBiFunction(String url, BiFunction<T, U, R> handler, MRestHandlerConfig config, HttpMethod... methodArr) {
        super(url, config, methodArr);
        this.handler = Objects.requireNonNull(handler);
    }

    @Override
    public BiFunction<T, U, R> getHandler() {
        return handler;
    }

    @Override
    public MRestHandlerType getType() {
        return MRestHandlerType.InputReqResp_Ret;
    }
}
