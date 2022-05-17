package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;

/**
 * @author jiashunx
 */
public interface MRestFilterChain {

    void doFilter(MRestRequest restRequest, MRestResponse restResponse);

}
