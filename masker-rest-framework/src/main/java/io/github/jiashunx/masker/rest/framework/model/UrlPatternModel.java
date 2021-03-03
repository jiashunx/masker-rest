package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;
import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

/**
 * @author jiashunx
 */
public class UrlPatternModel {

    private final String urlPattern;
    private final UrlPatternType urlPatternType;

    public UrlPatternModel(String pattern) {
        this.urlPattern = UrlParaser.getUrlPattern(pattern);
        this.urlPatternType = UrlParaser.getUrlPatternType(pattern);
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public UrlPatternType getUrlPatternType() {
        return urlPatternType;
    }

    public boolean isPatternStrictly() {
        return getUrlPatternType() == UrlPatternType.STRICTLY;
    }

    public boolean isPatternPathMatch() {
        return getUrlPatternType() == UrlPatternType.PATH_MATCH;
    }

    public boolean isPatternExt() {
        return getUrlPatternType() == UrlPatternType.EXT;
    }

}
