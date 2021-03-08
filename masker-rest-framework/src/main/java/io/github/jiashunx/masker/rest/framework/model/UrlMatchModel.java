package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;
import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

import java.util.Map;

/**
 * @author jiashunx
 */
public class UrlMatchModel {

    private final String url;
    private final String urlPattern;
    private final UrlPatternType urlPatternType;
    private final boolean matched;
    private final Map<String, String> placeholderMap;

    public UrlMatchModel(String url, String urlPattern) {
        this.url = UrlParaser.getUrl(url);
        this.urlPattern = UrlParaser.getUrlPattern(urlPattern);
        this.urlPatternType = UrlParaser.getUrlPatternType(urlPattern);
        this.matched = UrlParaser.isUrlMatchUrlPattern(this.url, this.urlPattern);
        this.placeholderMap = UrlParaser.getUrlPlaceholderMap(this.url, this.urlPattern);
    }

    public boolean isMatched() {
        return matched;
    }

    public String getUrl() {
        return url;
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

    public Map<String, String> getPlaceholderMap() {
        return placeholderMap;
    }
}
