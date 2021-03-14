package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
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
        if (StringUtils.isEmpty(url) || !url.startsWith(Constants.PATH_SEP)) {
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
            if (urlChar == Constants.PATH_SEP.charAt(0)) {
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

    public static final String PLACEHOLDER_START = "{";
    public static final String PLACEHOLDER_END = "}";
    public static final int PLACEHOLDER_START_LENGTH = PLACEHOLDER_START.length();
    public static final int PLACEHOLDER_END_LENGTH = PLACEHOLDER_END.length();
    public static final int PLACEHOLDER_GT_LENGTH = PLACEHOLDER_START_LENGTH + PLACEHOLDER_END_LENGTH;

    public static boolean isPlaceholderString(String string) {
        return string.length() > PLACEHOLDER_GT_LENGTH
                && string.startsWith(PLACEHOLDER_START) && string.endsWith(PLACEHOLDER_END)
                && string.indexOf(PLACEHOLDER_START) == string.lastIndexOf(PLACEHOLDER_START)
                && string.indexOf(PLACEHOLDER_END) == string.lastIndexOf(PLACEHOLDER_END);
    }

    public static String getPlaceholderName(String string) {
        if (isPlaceholderString(string)) {
            // 占位符字符串截取
            return string.substring(PLACEHOLDER_START_LENGTH, string.length() - PLACEHOLDER_END_LENGTH);
        }
        return StringUtils.EMPTY;
    }

    public static String getUrlPattern(String pattern) {
        String urlPattern = Objects.requireNonNull(pattern).trim();
        if (StringUtils.isEmpty(urlPattern)
                || urlPattern.indexOf(Constants.STRING_MATCH_ALL) != urlPattern.lastIndexOf(Constants.STRING_MATCH_ALL)) {
            throw new MRestMappingException(String.format("illegal urlPattern: %s", urlPattern));
        }
        return urlPattern;
    }

    public static UrlPatternType getUrlPatternType(String pattern) {
        String urlPattern = getUrlPattern(pattern);
        UrlPatternType patternType = null;
        if (!urlPattern.contains(Constants.STRING_MATCH_ALL) && urlPattern.startsWith(Constants.PATH_SEP)) {
            patternType = UrlPatternType.STRICTLY;
        } else if (urlPattern.startsWith(Constants.PATH_SEP)
                && urlPattern.endsWith(Constants.PATH_MATCH_ALL)
                && urlPattern.indexOf(Constants.STRING_MATCH_ALL) == urlPattern.lastIndexOf(Constants.STRING_MATCH_ALL)) {
            patternType = UrlPatternType.PATH_MATCH;
        } else if (urlPattern.startsWith(Constants.PATH_MATCH_ALL_PREFIX)
                && urlPattern.length() >= (Constants.PATH_MATCH_ALL_PREFIX.length() + 1)
                && urlPattern.indexOf(Constants.PATH_MATCH_ALL_PREFIX) == urlPattern.lastIndexOf(Constants.PATH_MATCH_ALL_PREFIX)
                && !urlPattern.substring(Constants.PATH_MATCH_ALL_PREFIX.length()).contains(Constants.STRING_MATCH_ALL)
                && !urlPattern.substring(Constants.PATH_MATCH_ALL_PREFIX.length()).contains(Constants.PATH_SEP)) {
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
                if (urlChar == Constants.PATH_SEP.charAt(0)) {
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
                        throw new MRestMappingException(String.format("illegal urlPattern: %s, conflict placeholder name: %s", pattern, name));
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
            String pattern = Constants.REGEX_PREFIX + urlPattern.replace(Constants.STRING_MATCH_ALL, Constants.REGEX_CHAR1N) + Constants.REGEX_SUFFIX;
            return url.matches(pattern);
        }
        if (urlPatternModel.isPatternPathMatch()) {
            String pattern = Constants.REGEX_PREFIX + urlPattern.replace(Constants.STRING_MATCH_ALL, Constants.REGEX_CHAR0N) + Constants.REGEX_SUFFIX;
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
