package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;

/**
 * @author jiashunx
 */
public class MRestFilterChainAdapter extends MRestFilterChainOfDefault {

    private final MRestFilterChain targetFilterChain;

    public MRestFilterChainAdapter(MRestFilter[] filterArr) {
        this(filterArr, null);
    }

    public MRestFilterChainAdapter(MRestFilter[] filterArr, MRestFilterChain targetFilterChain) {
        this(null, filterArr, targetFilterChain);
    }

    public MRestFilterChainAdapter(MRestContext restContext,  MRestFilter[] filterArr, MRestFilterChain targetFilterChain) {
        super(restContext, filterArr);
        this.targetFilterChain = targetFilterChain;
    }

    @Override
    public synchronized void doFilter(MRestRequest restRequest, MRestResponse restResponse) {
        super.doFilter(restRequest, restResponse);
        if (targetFilterChain != null) {
            targetFilterChain.doFilter(restRequest, restResponse);
        }
    }

}
