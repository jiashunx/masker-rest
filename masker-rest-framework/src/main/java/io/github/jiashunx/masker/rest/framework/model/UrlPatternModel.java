package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;

/**
 * @author jiashunx
 */
public class UrlPatternModel {

    private UrlPatternType urlPatternType;
    private String[] pathFieldArr;

    public UrlPatternType getUrlPatternType() {
        return urlPatternType;
    }

    public void setUrlPatternType(UrlPatternType urlPatternType) {
        this.urlPatternType = urlPatternType;
    }

    public String[] getPathFieldArr() {
        return pathFieldArr;
    }

    public void setPathFieldArr(String[] pathFieldArr) {
        this.pathFieldArr = pathFieldArr;
    }
}
