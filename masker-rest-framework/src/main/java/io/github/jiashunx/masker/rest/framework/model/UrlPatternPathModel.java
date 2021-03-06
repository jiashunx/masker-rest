package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

/**
 * @author jiashunx
 */
public class UrlPatternPathModel extends UrlPathModel {

    private final boolean placeholder;
    private final String placeholderName;
    private String originPath;
    private String originPathVal;
    private final boolean regular;

    public UrlPatternPathModel(String p) {
        super(p);
        placeholder = UrlParaser.isPlaceholderString(pathVal);
        placeholderName = UrlParaser.getPlaceholderName(pathVal);
        if (placeholder) {
            originPath = path;
            originPathVal = pathVal;
            path = Constants.PATH_MATCH_ALL;
            pathVal = Constants.STRING_MATCH_ALL;
        }
        regular = pathVal.endsWith(Constants.STRING_MATCH_ALL);
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }

    public String getOriginPath() {
        return originPath;
    }

    public String getOriginPathVal() {
        return originPathVal;
    }

    public boolean isRegular() {
        return regular;
    }
}
