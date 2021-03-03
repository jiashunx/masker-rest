package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.servlet.MRestServlet;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class UrlMappingServlet {

    private final UrlPatternModel urlPatternModel;
    private final MRestServlet restServlet;

    public UrlMappingServlet(UrlPatternModel urlPatternModel, MRestServlet restServlet) {
        this.urlPatternModel = Objects.requireNonNull(urlPatternModel);
        this.restServlet = Objects.requireNonNull(restServlet);
    }

    public UrlPatternModel getUrlPatternModel() {
        return urlPatternModel;
    }

    public MRestServlet getRestServlet() {
        return restServlet;
    }
}
