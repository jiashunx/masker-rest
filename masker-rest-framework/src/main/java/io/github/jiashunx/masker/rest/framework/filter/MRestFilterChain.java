package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestFilterChain {

    private final MRestServer restServer;
    private final MRestFilter[] filterArr;
    private int index;
    private final int size;

    public MRestFilterChain(MRestServer restServer, MRestFilter[] filterArr) {
        this.restServer = Objects.requireNonNull(restServer);
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

    public MRestServer getRestServer() {
        return restServer;
    }
}
