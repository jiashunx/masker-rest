package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.ServletMappingClass;
import io.github.jiashunx.masker.rest.framework.model.ServletMappingHandler;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.GetMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.PostMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.RequestMapping;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiashunx
 */
public abstract class AbstractRestServlet implements MRestServlet {

    private static final Map<String, ServletMappingClass> MAPPING_CLASS_MAP = new HashMap<>();

    private static ServletMappingClass getServletMappingClass(String requestUrl
            , Class<? extends AbstractRestServlet> servletClass) {
        ServletMappingClass mappingClass = MAPPING_CLASS_MAP.get(requestUrl);
        if (mappingClass == null) {
            synchronized (AbstractRestServlet.class) {
                mappingClass = MAPPING_CLASS_MAP.get(requestUrl);
                if (mappingClass == null) {
                    mappingClass = new ServletMappingClass(servletClass);
                    RequestMapping klassMapping = servletClass.getAnnotation(RequestMapping.class);
                    String klassMappingUrl = Constants.ROOT_PATH;
                    if (klassMapping != null) {
                        klassMappingUrl = MRestUtils.formatPath(klassMappingUrl);
                    }
                    Method[] methods = servletClass.getDeclaredMethods();
                    for (Method method: methods) {
                        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                        if (methodMapping != null) {
                            String methodMappingUrl = MRestUtils.formatPath(methodMapping.url());
                            if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                                methodMappingUrl = klassMappingUrl + methodMappingUrl;
                            }

                            ServletMappingHandler mappingHandler = new ServletMappingHandler(methodMappingUrl, method);

                        }
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        if (getMapping != null) {
                            String getMappingUrl = MRestUtils.formatPath(getMapping.url());
                            if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                                getMappingUrl = klassMappingUrl + getMappingUrl;
                            }
                        }
                        PostMapping postMapping = method.getAnnotation(PostMapping.class);
                        if (postMapping != null) {
                            String postMappingUrl = MRestUtils.formatPath(postMapping.url());
                            if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                                postMappingUrl = klassMappingUrl + postMappingUrl;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        Class<? extends AbstractRestServlet> servletClass = getClass();

    }

}
