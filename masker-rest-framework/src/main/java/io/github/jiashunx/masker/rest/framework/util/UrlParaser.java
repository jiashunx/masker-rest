package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.model.UrlModel;
import io.github.jiashunx.masker.rest.framework.model.UrlPathModel;
import io.github.jiashunx.masker.rest.framework.model.UrlPatternModel;
import io.github.jiashunx.masker.rest.framework.model.UrlPatternPathModel;
import io.github.jiashunx.masker.rest.framework.type.UrlPatternType;

import java.util.*;

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
        }
        return patternType;
    }

    public static UrlPatternType getUrlPatternTypeWithCheck(String pattern) {
        UrlPatternType patternType = getUrlPatternType(pattern);
        if (patternType == null) {
            throw new MRestMappingException(String.format("illegal urlPattern: %s", pattern));
        }
        return patternType;
    }

    public static List<UrlPatternPathModel> getUrlPatternPathModelList(String pattern) {
        UrlPatternType patternType = getUrlPatternType(pattern);
        if (patternType == UrlPatternType.STRICTLY || patternType == UrlPatternType.PATH_MATCH) {
            char[] urlCharArr = getUrl(pattern).toCharArray();
            List<UrlPatternPathModel> patternPathModelList = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int i = 0, length = urlCharArr.length; i < length; i++) {
                char urlChar = urlCharArr[i];
                if (urlChar == '/') {
                    int sbLen = sb.length();
                    if (sbLen > 0) {
                        patternPathModelList.add(new UrlPatternPathModel(sb.toString()));
                        sb.delete(0, sbLen);
                    }
                }
                sb.append(urlChar);
                if (i == length - 1) {
                    patternPathModelList.add(new UrlPatternPathModel(sb.toString()));
                }
            }
            // 占位符名称重复检查
            Set<String> set = new HashSet<>();
            for (UrlPatternPathModel patternPathModel: patternPathModelList) {
                if (patternPathModel.isPlaceholder()) {
                    String name = patternPathModel.getOriginPathVal();
                    if (set.contains(name)) {
                        throw new MRestMappingException(String.format("illegal urlPattern: %s, conflict placeholder name: %s"
                                , pattern, name));
                    }
                    set.add(name);
                }
            }
            return patternPathModelList;
        }
        return null;
    }

    public static Map<String, String> getUrlPlaceholderMap(String _url, String _urlPattern) {
        Map<String, String> kv = new HashMap<>();
        if (!isUrlMatchUrlPattern(_url, _urlPattern)) {
            return kv;
        }
        UrlModel urlModel = new UrlModel(_url);
        UrlPatternModel urlPatternModel = new UrlPatternModel(_urlPattern);
        if (urlPatternModel.isPatternStrictly() && urlPatternModel.isSupportPlaceholder()) {
            List<UrlPathModel> urlPathModelList = urlModel.getPathModelList();
            List<UrlPatternPathModel> urlPatternPathModelList = urlPatternModel.getPatternPathModelList();
            int pathModelListSize = urlModel.getPathModelListSize();
            int patternPathModelListSize = urlPatternModel.getPatternPathModelListSize();
            // url已确认和urlPattern匹配，且支持占位符处理
            for (int index = 0; index < pathModelListSize; index++) {
                UrlPathModel pathModel = urlPathModelList.get(index);
                UrlPatternPathModel patternPathModel = urlPatternPathModelList.get(index);
                if (patternPathModel.isPlaceholder()) {
                    kv.put(patternPathModel.getPlaceholderName(), pathModel.getPathVal());
                }
            }
        }
        return kv;
    }

    public static boolean isUrlMatchUrlPattern(String _url, String _urlPattern) {
        UrlModel urlModel = new UrlModel(_url);
        UrlPatternModel urlPatternModel = new UrlPatternModel(_urlPattern);
        String url = urlModel.getUrl();
        String urlPattern = urlPatternModel.getUrlPattern();
        if (urlPatternModel.isPatternExt()) {
            String pattern = "^" + urlPattern.replace("*", "\\S+") + "$";
            return url.matches(pattern);
        }
        if (urlPatternModel.isPatternPathMatch()) {
            String pattern = "^" + urlPattern.replace("*", "\\S*") + "$";
            return url.matches(pattern);
        }
        if (urlPatternModel.isPatternStrictly()) {
            if (urlPatternModel.isSupportPlaceholder()) {
                List<UrlPathModel> urlPathModelList = urlModel.getPathModelList();
                List<UrlPatternPathModel> urlPatternPathModelList = urlPatternModel.getPatternPathModelList();
                int pathModelListSize = urlModel.getPathModelListSize();
                int patternPathModelListSize = urlPatternModel.getPatternPathModelListSize();
                if (pathModelListSize == patternPathModelListSize) {
                    for (int index = 0; index < pathModelListSize; index++) {
                        UrlPathModel pathModel = urlPathModelList.get(index);
                        UrlPatternPathModel patternPathModel = urlPatternPathModelList.get(index);
                        if (!patternPathModel.isPlaceholder()
                                && !patternPathModel.getPathVal().equals(pathModel.getPathVal())) {
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                return url.equals(urlPattern);
            }
        }
        return false;
    }

}
