package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestFilterChainOfDefault implements MRestFilterChain {

    protected final MRestContext restContext;
    protected final MRestFilter[] filterArr;
    protected int index;

    public MRestFilterChainOfDefault(MRestFilter[] filterArr) {
        this(null, filterArr);
    }

    public MRestFilterChainOfDefault(MRestContext restContext, MRestFilter[] filterArr) {
        this.restContext = restContext;
        this.filterArr = Objects.requireNonNull(filterArr);
        this.index = 0;
    }

    @Override
    public synchronized void doFilter(MRestRequest restRequest, MRestResponse restResponse) {
        if (index < filterArr.length) {
            filterArr[index++].doFilter(restRequest, restResponse, this);
        }
    }

    public MRestContext getRestContext() {
        return restContext;
    }

}
