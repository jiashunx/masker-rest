package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
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
    private int patternPathModelListSize;
    private UrlPatternPathModel firstPatternPathModel;
    private UrlPatternPathModel lastPatternPathModel;
    private boolean supportPlaceholder;
    private boolean supportRegular;

    public UrlPatternModel(String pattern) {
        this.urlPattern = UrlParaser.getUrlPattern(pattern);
        this.urlPatternType = UrlParaser.getUrlPatternType(pattern);
        this.patternPathModelList = UrlParaser.getUrlPatternPathModelList(pattern);
        if (this.patternPathModelList != null && !this.patternPathModelList.isEmpty()) {
            this.patternPathModelListSize = patternPathModelList.size();
            for (UrlPatternPathModel patternPathModel: patternPathModelList) {
                if (patternPathModel.isPlaceholder()) {
                    this.supportPlaceholder = true;
                }
                if (patternPathModel.isRegular()) {
                    this.supportRegular = true;
                }
            }
            this.firstPatternPathModel = patternPathModelList.get(0);
            this.lastPatternPathModel = patternPathModelList.get(patternPathModelListSize - 1);
        }
        if (isPatternExt()) {
            this.supportRegular = true;
        }
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public UrlPatternType getUrlPatternType() {
        return urlPatternType;
    }

    public String getActualUrlPattern() {
        StringBuilder builder = new StringBuilder();
        for (UrlPatternPathModel urlPatternPathModel: getPatternPathModelList()) {
            builder.append(urlPatternPathModel.getPath());
        }
        return builder.toString();
    }

    public int getRegularCount() {
        if (isSupportRegular()) {
            return getActualUrlPattern().split(Constants.STRING_MATCH_ALL).length - 1;
        }
        return 0;
    }

    public int getActualPathMatchCount(String url) {
        List<UrlPathModel> urlPathModelList = UrlParaser.getUrlPathModelList(url);
        int size0 = urlPathModelList.size();
        int size1 = getPatternPathModelListSize();
        // 路径匹配度
        int pathMatchCount = 0;
        for (int i = 0; i < size0; i++) {
            if (i >= size1) {
                continue;
            }
            if (urlPathModelList.get(i).getPath().equals(getPatternPathModelList().get(i).getPath())) {
                pathMatchCount++;
            }
        }
        return pathMatchCount;
    }

    public List<UrlPatternPathModel> getPatternPathModelList() {
        return patternPathModelList;
    }

    public int getPatternPathModelListSize() {
        return patternPathModelListSize;
    }

    public UrlPatternPathModel getFirstPatternPathModel() {
        return firstPatternPathModel;
    }

    public UrlPatternPathModel getLastPatternPathModel() {
        return lastPatternPathModel;
    }

    public boolean isSupportPlaceholder() {
        return supportPlaceholder;
    }

    public boolean isSupportRegular() {
        return supportRegular;
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
