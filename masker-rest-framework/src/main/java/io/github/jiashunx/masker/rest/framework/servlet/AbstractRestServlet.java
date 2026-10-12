package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestHandleException;
import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.model.*;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.GetMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.HttpMethod;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.PostMapping;
import io.github.jiashunx.masker.rest.framework.servlet.mapping.RequestMapping;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.UrlParaser;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiashunx
 */
public abstract class AbstractRestServlet implements MRestServlet {

    private final Map<String, ServletMappingHandler> servletMappingHandlerMap = new HashMap<>();
    private volatile boolean initialized = false;

    public synchronized void init0() {
        Class<? extends AbstractRestServlet> servletClass = getClass();
        if (initialized) {
            return;
        }
        final RequestMapping requestMapping = servletClass.getAnnotation(RequestMapping.class);
        String klassMappingUrl = Constants.ROOT_PATH;
        if (requestMapping != null) {
            klassMappingUrl = MRestUtils.formatPath(requestMapping.url());
        }
        Method[] methods = servletClass.getDeclaredMethods();
        for (Method method: methods) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            if (getMapping != null) {
                String mappingUrl = MRestUtils.formatPath(getMapping.url());
                validateMappingMethod(servletClass, mappingUrl, method);
                if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                    mappingUrl = klassMappingUrl + mappingUrl;
                }
                ServletMappingHandler mappingHandler = servletMappingHandlerMap.get(mappingUrl);
                if (mappingHandler != null) {
                    if (!mappingHandler.getHandleMethod().equals(method)) {
                        throw new MRestMappingException(String.format("url [%s] mapping conflict, className.methodName=%s.%s"
                                , mappingUrl, servletClass.getName(), method.getName()));
                    }
                    mappingHandler.getMethods().add(HttpMethod.GET);
                } else {
                    mappingHandler = new ServletMappingHandler(servletClass, mappingUrl, method);
                    mappingHandler.getMethods().add(HttpMethod.GET);
                    servletMappingHandlerMap.put(mappingUrl, mappingHandler);
                }
            }
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            if (postMapping != null) {
                String mappingUrl = MRestUtils.formatPath(postMapping.url());
                validateMappingMethod(servletClass, mappingUrl, method);
                if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                    mappingUrl = klassMappingUrl + mappingUrl;
                }
                ServletMappingHandler mappingHandler = servletMappingHandlerMap.get(mappingUrl);
                if (mappingHandler != null) {
                    if (!mappingHandler.getHandleMethod().equals(method)) {
                        throw new MRestMappingException(String.format("url: %s mapping conflict, className.methodName=%s.%s"
                                , mappingUrl, servletClass.getName(), method.getName()));
                    }
                    mappingHandler.getMethods().add(HttpMethod.POST);
                } else {
                    mappingHandler = new ServletMappingHandler(servletClass, mappingUrl, method);
                    mappingHandler.getMethods().add(HttpMethod.POST);
                    servletMappingHandlerMap.put(mappingUrl, mappingHandler);
                }
            }
            RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
            if (methodRequestMapping != null) {
                String mappingUrl = MRestUtils.formatPath(methodRequestMapping.url());
                validateMappingMethod(servletClass, mappingUrl, method);
                if (!klassMappingUrl.equals(Constants.ROOT_PATH)) {
                    mappingUrl = klassMappingUrl + mappingUrl;
                }
                ServletMappingHandler mappingHandler = servletMappingHandlerMap.get(mappingUrl);
                if (mappingHandler != null) {
                    throw new MRestMappingException(String.format("url [%s] mapping conflict, className.methodName=%s.%s"
                            , mappingUrl, servletClass.getName(), method.getName()));
                }
                mappingHandler = new ServletMappingHandler(servletClass, mappingUrl, method);
                mappingHandler.getMethods().addAll(Arrays.asList(methodRequestMapping.method()));
                servletMappingHandlerMap.put(mappingUrl, mappingHandler);
            }
        }
        initialized = true;
    }

    private static void validateMappingMethod(Class<?> servletClass, String mappingUrl, Method method) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            throw new MRestMappingException(String.format("url [%s] mapping method must be public, className.methodName=%s.%s"
                    , mappingUrl, servletClass.getName(), method.getName()));
        }
        if (Modifier.isStatic(modifiers)) {
            throw new MRestMappingException(String.format("url [%s] mapping method must not be static, className.methodName=%s.%s"
                    , mappingUrl, servletClass.getName(), method.getName()));
        }
    }

    /**
     * servlet处理实例缓存, 键为servlet处理类.
     * 使用ConcurrentHashMap保证多worker线程并发分发时的线程安全.
     */
    private final Map<Class<? extends MRestServlet>, MRestServlet> servletHandlerMap = new ConcurrentHashMap<>();

    public void init() {
        // 快速路径: 已初始化则直接返回, 避免每次请求都获取锁.
        if (initialized) {
            return;
        }
        init0();
    }

    private MRestServlet getServletHandlerInstance(Class<? extends MRestServlet> servletHandlerClass) {
        return servletHandlerMap.computeIfAbsent(servletHandlerClass, klass -> {
            try {
                return klass.getConstructor(this.getClass()).newInstance(this);
            } catch (Throwable throwable) {
                throw new MRestHandleException(String.format("create servlet mapping handler instance failed, class: %s", klass.getName()), throwable);
            }
        });
    }

    public List<String> getMappingUrlList() {
        init();
        return new ArrayList<>(servletMappingHandlerMap.keySet());
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        init();
        String requestUrl = restRequest.getUrl();
        List<String> urlList = getMappingUrlList();
        String matchedPattern = null;
        for (String _patternUrl: urlList) {
            if (UrlParaser.isUrlMatchUrlPattern(requestUrl, _patternUrl)) {
                matchedPattern = _patternUrl;
                break;
            }
        }
        ServletMappingHandler mappingHandler = servletMappingHandlerMap.get(matchedPattern);
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
            throw new MRestHandleException(String.format("url: %s method mapping handler is not public.", requestUrl));
        }
        try {
            Class<? extends MRestServlet> servletHandlerClass = mappingHandler.getServletHandlerClass();
            MRestServlet servletInstance = getServletHandlerInstance(servletHandlerClass);
            servletInstance.service(restRequest, restResponse);
        } catch (Throwable throwable) {
            throw new MRestHandleException(throwable);
        }
    }

}
