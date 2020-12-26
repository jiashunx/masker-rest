package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MWebsocketRequest;
import io.github.jiashunx.masker.rest.framework.MWebsocketResponse;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

/**
 * @author jiashunx
 */
public interface MWebsocketHandler<Frame extends WebSocketFrame> {

    void execute(Frame websocketFrame, MWebsocketRequest websocketRequest, MWebsocketResponse websocketResponse);

}
