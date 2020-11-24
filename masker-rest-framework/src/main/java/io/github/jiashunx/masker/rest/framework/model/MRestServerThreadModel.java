package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestServerThreadModel {

    private MRestServer restServer;
    private MRestRequest restRequest;
    private MRestResponse restResponse;

    public MRestServerThreadModel assertNotNull() {
        Objects.requireNonNull(restServer);
        Objects.requireNonNull(restRequest);
        Objects.requireNonNull(restResponse);
        return this;
    }

    public MRestServer getRestServer() {
        return restServer;
    }

    public void setRestServer(MRestServer restServer) {
        this.restServer = restServer;
    }

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
}
