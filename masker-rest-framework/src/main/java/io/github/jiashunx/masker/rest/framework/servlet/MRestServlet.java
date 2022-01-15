package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;

/**
 * @author jiashunx
 */
public interface MRestServlet {

    void service(MRestRequest restRequest, MRestResponse restResponse);

    default MRestServlet getInstance() {
        return this;
    }

    default String urlPattern() {
        if (!hasServletAnnotation()) {
            return Constants.DEFAULT_SERVLET_URLPATTERN;
        }
        return getServletAnnotation().urlPattern();
    }

    default String servletName() {
        return getClass().getName();
    }

    default boolean hasServletAnnotation() {
        return getServletAnnotation() != null;
    }

    default MServlet getServletAnnotation() {
        return getInstance().getClass().getAnnotation(MServlet.class);
    }

}
