package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LastServlet implements MRestServlet {

    private static final Logger logger = LoggerFactory.getLogger(LastServlet.class);
    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        if (restResponse.isWriteMethodInvoked()) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} url not found: [{}]", restRequest.getRestContext().getContextDesc(), restRequest.getUrl());
        }
        if (restRequest.getMethod() == HttpMethod.GET) {
            restResponse.writeStatusPage(HttpResponseStatus.NOT_FOUND);
        } else {
            restResponse.write(HttpResponseStatus.NOT_FOUND);
        }
    }

}
