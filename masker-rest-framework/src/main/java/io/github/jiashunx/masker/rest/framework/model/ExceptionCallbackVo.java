package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author jiashunx
 */
public class ExceptionCallbackVo {

    private MRestRequest restRequest;
    private MRestResponse restResponse;
    private Throwable throwable;
    private ChannelHandlerContext channelHandlerContext;

    public MRestRequest getRestRequest() {
        return restRequest;
    }

    public void setRestRequest(MRestRequest restRequest) {
        this.restRequest = restRequest;
    }

    public MRestResponse getRestResponse() {
        return restResponse;
    }

    public void setRestResponse(MRestResponse restResponse) {
        this.restResponse = restResponse;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }
}
