package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestHandleException;
import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.model.ServletMappingClass;
import io.github.jiashunx.masker.rest.framework.model.ServletMappingHandler;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.GetMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.HttpMethod;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.PostMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.RequestMapping;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
                    final RequestMapping requestMapping = servletClass.getAnnotation(RequestMapping.class);
                    String klassMappingUrl = Constants.ROOT_PATH;
                    if (requestMapping != null) {
                        klassMappingUrl = MRestUtils.formatPath(requestMapping.url());
                    }
                    Method[] methods = servletClass.getDeclaredMethods();
                    for (Method method: methods) {
                        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                        if (methodRequestMapping != null) {
                            String mappingUrl = MRestUtils.formatPath(methodRequestMapping.url());
                            if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                                mappingUrl = klassMappingUrl + mappingUrl;
                            }
                            ServletMappingHandler mappingHandler = mappingClass.getMappingHandler(mappingUrl);
                            if (mappingHandler != null) {
                                throw new MRestMappingException(String.format("url [%s] mapping conflict, className.methodName=%s.%s"
                                        , mappingUrl, servletClass.getName(), method.getName()));
                            }
                            mappingHandler = new ServletMappingHandler(mappingUrl, method);
                            mappingHandler.getMethods().addAll(Arrays.asList(HttpMethod.values()));
                            mappingClass.putMappingHandler(mappingUrl, mappingHandler);
                        }
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        if (getMapping != null) {
                            String mappingUrl = MRestUtils.formatPath(getMapping.url());
                            if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                                mappingUrl = klassMappingUrl + mappingUrl;
                            }
                            ServletMappingHandler mappingHandler = mappingClass.getMappingHandler(mappingUrl);
                            if (mappingHandler != null) {
                                if (!mappingHandler.getHandleMethod().equals(method)) {
                                    throw new MRestMappingException(String.format("url [%s] mapping conflict, className.methodName=%s.%s"
                                            , mappingUrl, servletClass.getName(), method.getName()));
                                }
                                mappingHandler.getMethods().add(HttpMethod.GET);
                            } else {
                                mappingHandler = new ServletMappingHandler(mappingUrl, method);
                                mappingHandler.getMethods().add(HttpMethod.GET);
                                mappingClass.putMappingHandler(mappingUrl, mappingHandler);
                            }
                        }
                        PostMapping postMapping = method.getAnnotation(PostMapping.class);
                        if (postMapping != null) {
                            String mappingUrl = MRestUtils.formatPath(postMapping.url());
                            if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                                mappingUrl = klassMappingUrl + mappingUrl;
                            }
                            ServletMappingHandler mappingHandler = mappingClass.getMappingHandler(mappingUrl);
                            if (mappingHandler != null) {
                                if (!mappingHandler.getHandleMethod().equals(method)) {
                                    throw new MRestMappingException(String.format("url: %s mapping conflict, className.methodName=%s.%s"
                                            , mappingUrl, servletClass.getName(), method.getName()));
                                }
                                mappingHandler.getMethods().add(HttpMethod.POST);
                            } else {
                                mappingHandler = new ServletMappingHandler(mappingUrl, method);
                                mappingHandler.getMethods().add(HttpMethod.POST);
                                mappingClass.putMappingHandler(mappingUrl, mappingHandler);
                            }
                        }
                    }
                    AtomicReference<ServletMappingClass> mappingClassRef = new AtomicReference<>(mappingClass);
                    List<String> mappingUrls = mappingClass.getMappingUrls();
                    mappingUrls.forEach(mappingUrl -> {
                        if (MAPPING_CLASS_MAP.containsKey(mappingUrl)) {
                            throw new MRestMappingException(String.format("url: %s mapping conflict.", mappingUrl));
                        }
                        MAPPING_CLASS_MAP.put(mappingUrl, mappingClassRef.get());
                    });
                }
            }
        }
        return mappingClass;
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        String requestUrl = restRequest.getUrl();
        Class<? extends AbstractRestServlet> servletClass = getClass();
        ServletMappingClass mappingClass = getServletMappingClass(requestUrl, servletClass);
        ServletMappingHandler mappingHandler = mappingClass.getMappingHandler(requestUrl);
        if (mappingHandler == null) {
            restResponse.writeStatusPageAsHtml(HttpResponseStatus.NOT_FOUND);
            return;
        }
        HttpMethod method = HttpMethod.valueOf(restRequest.getMethod().name());
        if (!mappingHandler.getMethods().contains(method)) {
            restResponse.writeStatusPageAsHtml(HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }
        Method handleMethod = mappingHandler.getHandleMethod();
        if (!Modifier.isPublic(handleMethod.getModifiers())) {
            throw new MRestHandleException("url: %s method mapping handler is not public.");
        }
        try {
            Object[] arguments = null;
            switch (mappingHandler.getHandlerType()) {
                case InputReq_NoRet:
                    arguments = new Object[]{restRequest};
                    break;
                case InputReqResp_NoRet:
                    arguments = new Object[]{restRequest, restResponse};
                    break;
                default:
                    arguments = new Object[0];
                    break;
            }
            handleMethod.invoke(this, arguments);
        } catch (Throwable throwable) {
            throw new MRestHandleException(throwable);
        }
    }

}
