package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;

/**
 * @author jiashunx
 */
public abstract class MRestServletAdapter implements MRestFilter {

    @Override
    public abstract void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain);
}
