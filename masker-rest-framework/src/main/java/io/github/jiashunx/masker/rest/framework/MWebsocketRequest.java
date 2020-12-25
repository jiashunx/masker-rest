package io.github.jiashunx.masker.rest.framework;

import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * @author jiashunx
 */
public class MWebsocketRequest {

    private MWebsocketContext websocketContext;
    private String protocolName;
    private String protocolVersion;
    private String clientAddress;
    private int clientPort;
    private String remoteAddress;
    private int remotePort;
    private String contextPath;
    private WebSocketServerHandshaker handshaker;

    public MWebsocketContext getWebsocketContext() {
        return websocketContext;
    }

    public void setWebsocketContext(MWebsocketContext websocketContext) {
        this.websocketContext = websocketContext;
    }

    public String getProtocolName() {
        return protocolName;
    }

    public String getProtocolNameLowerCase() {
        return getProtocolName().toLowerCase();
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public String getProtocolVersionLowerCase() {
        return getProtocolVersion().toLowerCase();
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public WebSocketServerHandshaker getHandshaker() {
        return handshaker;
    }

    public void setHandshaker(WebSocketServerHandshaker handshaker) {
        this.handshaker = handshaker;
    }
}
