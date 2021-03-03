package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.model.UrlModel;
import io.github.jiashunx.masker.rest.framework.model.UrlPathModel;
import io.github.jiashunx.masker.rest.framework.model.UrlPatternModel;
import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class UrlParaser {

    private UrlParaser() {}

    public static UrlModel parseRequestUrl(String requestUrl) {
        UrlModel urlModel = new UrlModel(Objects.requireNonNull(requestUrl));
        char[] urlCharArr = requestUrl.toCharArray();
        List<UrlPathModel> urlPathList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = urlCharArr.length; i < length; i++) {
            char urlChar = urlCharArr[i];
            if (urlChar == '/') {
                int sbLen = sb.length();
                if (sbLen > 0) {
                    urlPathList.add(new UrlPathModel(sb.toString()));
                    sb.delete(0, sbLen);
                }
            }
            sb.append(urlChar);
            if (i == length - 1) {
                urlPathList.add(new UrlPathModel(sb.toString()));
            }
        }
        urlModel.setPathList(urlPathList);
        return urlModel;
    }

    public static UrlPatternModel parseUrlPattern(String urlPattern) {
        if (urlPattern.indexOf("*") != urlPattern.lastIndexOf("*")) {
            throw new MRestMappingException(String.format("illegal urlPattern: %s", urlPattern));
        }
        UrlPatternType urlPatternType = null;
        if (!urlPattern.contains("*") && urlPattern.startsWith("/")) {
            urlPatternType = UrlPatternType.STRICTLY;
        } else if (urlPattern.startsWith("/") && urlPattern.endsWith("/*")) {
            urlPatternType = UrlPatternType.PATH_MATCH;
        } else if (urlPattern.startsWith("*.") && urlPattern.length() >= 3) {
            urlPatternType = UrlPatternType.EXT;
        } else {
            throw new MRestMappingException(String.format("illegal urlPattern: %s", urlPattern));
        }
        UrlPatternModel urlPatternModel = new UrlPatternModel();
        urlPatternModel.setUrlPatternType(urlPatternType);
        return null;
    }

}
