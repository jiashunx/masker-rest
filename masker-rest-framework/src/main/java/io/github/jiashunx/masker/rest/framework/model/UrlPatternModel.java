package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;
import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UrlPatternModel that = (UrlPatternModel) object;
        return supportPlaceholder == that.supportPlaceholder &&
                /*Objects.equals(urlPattern, that.urlPattern) &&*/
                urlPatternType == that.urlPatternType &&
                Objects.equals(patternPathModelList, that.patternPathModelList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(/*urlPattern, */urlPatternType, patternPathModelList, supportPlaceholder);
    }
}
