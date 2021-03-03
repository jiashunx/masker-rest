package io.github.jiashunx.masker.rest.framework.model;

/**
 * @author jiashunx
 */
public class UrlPatternPathModel extends UrlPathModel {

    private boolean placeholder;
    private String placeholderName = "";

    public UrlPatternPathModel(String p) {
        super(p);
        placeholder = false;
        placeholderName = "";
        if (pathVal.length() > 2
                && pathVal.startsWith("{") && pathVal.endsWith("}")
                && pathVal.indexOf("{") == pathVal.lastIndexOf("{")
                && pathVal.indexOf("}") == pathVal.lastIndexOf("}")) {
            placeholder = true;
            placeholderName = pathVal.substring(1, pathVal.length() - 1);
        }
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public String getPlaceholderName() {
        return placeholderName;
    }
}
