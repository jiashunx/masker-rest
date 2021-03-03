package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;
import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

import java.util.List;

/**
 * @author jiashunx
 */
public class UrlPatternModel {

    private final String urlPattern;
    private final UrlPatternType urlPatternType;
    private final List<UrlPatternPathModel> patternPathModelList;
    private boolean supportPlaceholder;

    public UrlPatternModel(String pattern) {
        this.urlPattern = UrlParaser.getUrlPattern(pattern);
        this.urlPatternType = UrlParaser.getUrlPatternTypeWithCheck(pattern);
        this.patternPathModelList = UrlParaser.getUrlPatternPathModelList(pattern);
        if (this.patternPathModelList != null) {
            for (UrlPatternPathModel patternPathModel: patternPathModelList) {
                if (patternPathModel.isPlaceholder()) {
                    this.supportPlaceholder = true;
                    break;
                }
            }
        }
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public UrlPatternType getUrlPatternType() {
        return urlPatternType;
    }

    public List<UrlPatternPathModel> getPatternPathModelList() {
        return patternPathModelList;
    }

    public boolean isSupportPlaceholder() {
        return supportPlaceholder;
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
