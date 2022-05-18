package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;

/**
 * @author jiashunx
 */
public class MRestFilterChainAdapter extends MRestFilterChainOfDefault {

    private final MRestFilterChain targetFilterChain;

    public MRestFilterChainAdapter(MRestFilter... filterArr) {
        this(null, filterArr);
    }

    public MRestFilterChainAdapter(MRestFilterChain targetFilterChain, MRestFilter... filterArr) {
        this(targetFilterChain, null, filterArr);
    }

    public MRestFilterChainAdapter(MRestFilterChain targetFilterChain, MRestContext restContext, MRestFilter[] filterArr) {
        super(restContext, filterArr);
        this.targetFilterChain = targetFilterChain;
    }

    @Override
    public synchronized void doFilter(MRestRequest restRequest, MRestResponse restResponse) {
        if (index < filterArr.length) {
            filterArr[index++].doFilter(restRequest, restResponse, this);
            // 这里的return就很有灵性~
            return;
        }
        if (targetFilterChain != null) {
            targetFilterChain.doFilter(restRequest, restResponse);
        }
    }

}
