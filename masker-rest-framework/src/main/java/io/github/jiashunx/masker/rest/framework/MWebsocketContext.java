package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.util.MRestUtils;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MWebsocketContext {

    private final MRestServer restServer;
    private final String contextPath;

    public MWebsocketContext(MRestServer restServer, String contextPath) {
        this.restServer = Objects.requireNonNull(restServer);
        this.contextPath = MRestUtils.formatContextPath(contextPath);
    }

    public void init() {

    }

}
