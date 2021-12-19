package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

public class LastServlet implements MRestServlet {

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        if (restResponse.isWriteMethodInvoked()) {
            return;
        }
        if (restRequest.getMethod() == HttpMethod.GET) {
            restResponse.writeStatusPage(HttpResponseStatus.NOT_FOUND);
        } else {
            restResponse.write(HttpResponseStatus.NOT_FOUND);
        }
    }

}
