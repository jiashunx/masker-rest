package io.github.jiashunx.masker.rest.framework;

import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * @author jiashunx
 */
public class MWebsocketRequest extends MRestRequest {

    private MWebsocketContext websocketContext;
    private WebSocketServerHandshaker handshaker;

    public MWebsocketRequest() {}

    public MWebsocketRequest(MRestRequest restRequest) {
        super(restRequest);
    }

    public MWebsocketContext getWebsocketContext() {
        return websocketContext;
    }

    public void setWebsocketContext(MWebsocketContext websocketContext) {
        this.websocketContext = websocketContext;
    }

    public WebSocketServerHandshaker getHandshaker() {
        return handshaker;
    }

    public void setHandshaker(WebSocketServerHandshaker handshaker) {
        this.handshaker = handshaker;
    }

}
