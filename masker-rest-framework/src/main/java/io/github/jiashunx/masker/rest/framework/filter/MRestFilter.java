package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;

/**
 * @author jiashunx
 */
public interface MRestFilter {

    /**
     * doFilter.
     * @param restRequest MRestRequest
     * @param restResponse MRestResponse
     * @param filterChain MRestFilterChain
     */
    void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain);

    /**
     * 获取当前filter对象.
     * @return MRestFilter
     */
    default MRestFilter getInstance() {
        return this;
    }

    /**
     * 获取filter顺序.
     * @return int
     */
    default int order() {
        Filter annotation = getFilterAnnotation();
        if (annotation != null) {
            return annotation.order();
        }
        return Constants.DEFAULT_FILTER_ORDER;
    }

    /**
     * 获取url匹配列表
     * @return String[]
     */
    default String[] urlPatterns() {
        if (!hasFilterAnnotation()) {
            return Constants.DEFAULT_FILTER_URLPATTERNS;
        }
        return getFilterAnnotation().urlPatterns();
    }

    /**
     * 判断当前filter对象是否有@Filter注解
     * @return boolean
     */
    default boolean hasFilterAnnotation() {
        return getFilterAnnotation() != null;
    }

    /**
     * 获取当前filter对象的@Filter注解
     * @return Filter
     */
    default Filter getFilterAnnotation() {
        return getInstance().getClass().getAnnotation(Filter.class);
    }

}
