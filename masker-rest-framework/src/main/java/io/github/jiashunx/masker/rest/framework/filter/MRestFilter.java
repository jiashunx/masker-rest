package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;

/**
 * @author jiashunx
 */
public interface MRestFilter {

    void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain);

    default MRestFilter getInstance() {
        return this;
    }

    default int order() {
        Filter annotation = getFilterAnnotation();
        if (annotation != null) {
            return annotation.order();
        }
        return Constants.DEFAULT_FILTER_ORDER;
    }

    default String[] urlPatterns() {
        if (!hasFilterAnnotation()) {
            return Constants.DEFAULT_FILTER_URLPATTERNS;
        }
        return getFilterAnnotation().urlPatterns();
    }

    default boolean hasFilterAnnotation() {
        return getFilterAnnotation() != null;
    }

    default Filter getFilterAnnotation() {
        return getInstance().getClass().getAnnotation(Filter.class);
    }

}
