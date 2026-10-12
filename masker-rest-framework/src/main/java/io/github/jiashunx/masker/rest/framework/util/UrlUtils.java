package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;

public class UrlUtils {

    /**
     * 修正url, null转为空字符串
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String nullToEmpty(String url0) {
        if (url0 == null) {
            return StringUtils.EMPTY;
        }
        return url0;
    }

    /**
     * 替换url中windows路径分隔符
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String replaceWinSep(String url0) {
        return nullToEmpty(url0).replace(Constants.PATH_SEP_WIN, Constants.PATH_SEP);
    }

    /**
     * 移除url前缀"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removePrefixSep0(String url0) {
        String url = nullToEmpty(url0);
        if (url.startsWith(Constants.PATH_SEP)) {
            url = url.substring(1);
        }
        return url;
    }

    /**
     * 移除url前缀"/", 保留最后一位"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removePrefixSep(String url0) {
        String url = nullToEmpty(url0);
        if (url.startsWith(Constants.PATH_SEP) && url.length() > 1) {
            if (url.charAt(1) == Constants.CHAR_PATH_SEP) {
                url = removePrefixSep0(url);
            }
        }
        return url;
    }

    /**
     * 循环移除url前缀"/", 不保留最后一位"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removePrefixSeps0(String url0) {
        String url = nullToEmpty(url0);
        while (url.startsWith(Constants.PATH_SEP)) {
            url = removePrefixSep0(url);
        }
        return url;
    }

    /**
     * 循环移除url前缀"/", 保留最后一位"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removePrefixSeps(String url0) {
        String url = nullToEmpty(url0);
        while (url.startsWith(Constants.PATH_SEP)) {
            if (url.length() == 1 || url.charAt(1) != Constants.CHAR_PATH_SEP) {
                break;
            }
            url = removePrefixSep0(url);
        }
        return url;
    }

    /**
     * 移除url后缀"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removeSuffixSep0(String url0) {
        String url = nullToEmpty(url0);
        if (url.endsWith(Constants.PATH_SEP)) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * 移除url后缀"/", 保留最后一位"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removeSuffixSep(String url0) {
        String url = nullToEmpty(url0);
        if (url.endsWith(Constants.PATH_SEP) && url.length() > 1) {
            if (url.charAt(url.length() - 2) == Constants.CHAR_PATH_SEP) {
                url = removeSuffixSep0(url);
            }
        }
        return url;
    }

    /**
     * 循环移除url后缀"/", 不保留最后一位"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removeSuffixSeps0(String url0) {
        String url = nullToEmpty(url0);
        while (url.endsWith(Constants.PATH_SEP)) {
            url = removeSuffixSep0(url);
        }
        return url;
    }

    /**
     * 循环移除url后缀"/", 保留最后一位"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String removeSuffixSeps(String url0) {
        String url = nullToEmpty(url0);
        while (url.endsWith(Constants.PATH_SEP)) {
            if (url.length() == 1 || url.charAt(url.length() - 2) != Constants.CHAR_PATH_SEP) {
                break;
            }
            url = removeSuffixSep0(url);
        }
        return url;
    }

    /**
     * 向url添加前缀"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String appendPrefixSep(String url0) {
        String url = nullToEmpty(url0);
        if (!url.startsWith(Constants.PATH_SEP)) {
            url = Constants.PATH_SEP + url;
        }
        return url;
    }

    /**
     * 向url添加后缀"/"
     * @param url0 原始url
     * @return 修正后的url
     */
    public static String appendSuffixSep(String url0) {
        String url = nullToEmpty(url0);
        if (!url.endsWith(Constants.PATH_SEP)) {
            url = url + Constants.PATH_SEP;
        }
        return url;
    }

    /**
     * 判断请求路径是否命中指定路径前缀, 需满足路径边界(避免 /abc 命中前缀 /a).
     * @param requestPath 请求路径
     * @param prefixPath 前缀路径
     * @return boolean
     */
    public static boolean isPathPrefixMatch(String requestPath, String prefixPath) {
        String path = nullToEmpty(requestPath);
        String prefix = nullToEmpty(prefixPath);
        return path.equals(prefix) || path.startsWith(prefix + Constants.PATH_SEP);
    }

    /**
     * 判断路径中是否包含父级目录段(".."), 用于防止路径穿越.
     * @param path0 路径
     * @return boolean
     */
    public static boolean containsParentDirSegment(String path0) {
        String path = nullToEmpty(path0);
        if (path.isEmpty()) {
            return false;
        }
        for (String segment: path.split(Constants.PATH_SEP)) {
            if ("..".equals(segment)) {
                return true;
            }
        }
        return false;
    }

}
