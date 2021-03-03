package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.UrlModel;

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
        List<String> urlPathList = new ArrayList<>();
        List<String> urlPathValList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, length = urlCharArr.length; i < length; i++) {
            char urlChar = urlCharArr[i];
            if (urlChar == '/') {
                int sbLen = sb.length();
                if (sbLen > 0) {
                    urlPathList.add(sb.toString());
                    sb.delete(0, sbLen);
                }
            }
            sb.append(urlChar);
            if (i == length - 1) {
                urlPathList.add(sb.toString());
            }
        }
        urlModel.setPathList(urlPathList);
        urlPathList.forEach(urlPath -> {
            if (urlPath.equals("/")) {
                urlPathValList.add("");
            } else {
                urlPathValList.add(urlPath.substring(1));
            }
        });
        urlModel.setPathValList(urlPathValList);
        return urlModel;
    }

}
