package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

import java.util.Map;

/**
 * @author jiashunx
 */
public class UrlMatchModel {

    private final String url;
    private final String urlPattern;
    private final UrlPatternModel urlPatternModel;
    private final boolean matched;
    private final Map<String, String> placeholderMap;

    public UrlMatchModel(String url, String urlPattern) {
        this.url = UrlParaser.getUrl(url);
        this.urlPattern = UrlParaser.getUrlPattern(urlPattern);
        this.urlPatternModel = new UrlPatternModel(this.urlPattern);
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

    public UrlPatternModel getUrlPatternModel() {
        return urlPatternModel;
    }

    public boolean isPatternStrictly() {
        return getUrlPatternModel().isPatternStrictly();
    }

    public boolean isPatternPathMatch() {
        return getUrlPatternModel().isPatternPathMatch();
    }

    public boolean isPatternExt() {
        return getUrlPatternModel().isPatternExt();
    }

    public Map<String, String> getPlaceholderMap() {
        return placeholderMap;
    }
}
