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

    public static String getUrl(String url) {
        url = Objects.requireNonNull(url).trim();
        if (StringUtils.isEmpty(url) || !url.startsWith("/")) {
            throw new IllegalArgumentException(String.format("illegal url: \"%s\"", url));
        }
        return url;
    }

    public static List<UrlPathModel> getUrlPathModelList(String requestUrl) {
        char[] urlCharArr = getUrl(requestUrl).toCharArray();
        List<UrlPathModel> pathModelList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = urlCharArr.length; i < length; i++) {
            char urlChar = urlCharArr[i];
            if (urlChar == '/') {
                int sbLen = sb.length();
                if (sbLen > 0) {
                    pathModelList.add(new UrlPathModel(sb.toString()));
                    sb.delete(0, sbLen);
                }
            }
            sb.append(urlChar);
            if (i == length - 1) {
                pathModelList.add(new UrlPathModel(sb.toString()));
            }
        }
        return pathModelList;
    }

    public static boolean isPlaceholderString(String string) {
        return string.length() > 2
                && string.startsWith("{") && string.endsWith("}")
                && string.indexOf("{") == string.lastIndexOf("{")
                && string.indexOf("}") == string.lastIndexOf("}");
    }

    public static String getPlaceholderName(String string) {
        if (isPlaceholderString(string)) {
            return string.substring(1, string.length() - 1);
        }
        return "";
    }

    public static String getUrlPattern(String pattern) {
        String urlPattern = Objects.requireNonNull(pattern).trim();
        if (StringUtils.isEmpty(urlPattern)
                || urlPattern.indexOf("*") != urlPattern.lastIndexOf("*")) {
            throw new MRestMappingException(String.format("illegal urlPattern: %s", urlPattern));
        }
        return urlPattern;
    }

    public static UrlPatternType getUrlPatternType(String pattern) {
        String urlPattern = getUrlPattern(pattern);
        UrlPatternType patternType = null;
        if (!urlPattern.contains("*") && urlPattern.startsWith("/")) {
            patternType = UrlPatternType.STRICTLY;
        } else if (urlPattern.startsWith("/") && urlPattern.endsWith("/*")) {
            patternType = UrlPatternType.PATH_MATCH;
        } else if (urlPattern.startsWith("*.") && urlPattern.length() >= 3
                && !urlPattern.substring(2).contains("/")) {
            patternType = UrlPatternType.EXT;
        } else {
            throw new MRestMappingException(String.format("illegal urlPattern: %s", urlPattern));
        }
        return patternType;
    }

    public static UrlPatternModel parseUrlPattern(String pattern) {
        UrlPatternModel urlPatternModel = new UrlPatternModel(pattern);

        return null;
    }

}
