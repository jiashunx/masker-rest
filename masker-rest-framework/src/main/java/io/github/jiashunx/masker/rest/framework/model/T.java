package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.exception.MRestHandleException;
import io.github.jiashunx.masker.rest.framework.servlet.MRestServlet;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author jiashunx
 */
public class T {

    public final Map<Class<? extends MRestServlet>, MRestServlet> servletHandlerMap = new WeakHashMap<>();

    public synchronized void init() {
        if (initialized) {
            return;
        }
        init0();
    }

    private MRestServlet getServletHandlerInstance(Class<? extends MRestServlet> servletHandlerClass) {
        MRestServlet servletInstance = servletHandlerMap.get(servletHandlerClass);
        if (servletInstance == null) {
            synchronized (servletHandlerMap) {
                final MRestServlet servletInstance0 = servletHandlerMap.get(servletHandlerClass);
                if (servletInstance0 == null) {
                    try {
                        servletInstance = servletHandlerClass.getConstructor(this.getClass()).newInstance(this);
                    } catch (Throwable throwable) {
                        throw new MRestHandleException(String.format("create servlet mapping handler instance failed, class: %s", servletHandlerClass.getName()), throwable);
                    }
                    servletHandlerMap.put(servletHandlerClass, servletInstance);
                } else {
                    servletInstance = servletInstance0;
                }
            }
        }
        return servletInstance;
    }


}
