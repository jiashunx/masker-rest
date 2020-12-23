package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestFilterChain {

    private final MRestContext restContext;
    private final MRestFilter[] filterArr;
    private int index;
    private final int size;

    public MRestFilterChain(MRestContext restContext, MRestFilter[] filterArr) {
        this.restContext = Objects.requireNonNull(restContext);
        this.filterArr = Objects.requireNonNull(filterArr);
        this.size = this.filterArr.length;
        this.index = 0;
    }

    public void doFilter(MRestRequest restRequest, MRestResponse restResponse) {
        MRestFilter filter = next();
        if (filter != null) {
            filter.doFilter(restRequest, restResponse, this);
        }
    }

    private MRestFilter next() {
        MRestFilter filter = null;
        if (index < size) {
            filter = filterArr[index];
            index++;
        }
        return filter;
    }

    public MRestContext getRestContext() {
        return restContext;
    }

}
