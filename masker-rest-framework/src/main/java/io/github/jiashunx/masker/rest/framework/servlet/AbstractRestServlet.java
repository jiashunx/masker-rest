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
import io.netty.handler.codec.http.HttpResponseStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jiashunx
 */
public abstract class AbstractRestServlet implements MRestServlet {

    private final Map<String, ServletMappingClass> MAPPING_CLASS_MAP = new HashMap<>();
    private final Map<Class<?>, Boolean> INIT_STATE_MAP = new HashMap<>();

    public synchronized void init0() {
        Class<? extends AbstractRestServlet> servletClass = getClass();
        Boolean initialized = INIT_STATE_MAP.get(servletClass);
        if (initialized != null && initialized) {
            return;
        }
        ServletMappingClass mappingClass = new ServletMappingClass(servletClass);
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
                mappingHandler = new ServletMappingHandler(servletClass, mappingUrl, method);
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
                    mappingHandler = new ServletMappingHandler(servletClass, mappingUrl, method);
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
                    mappingHandler = new ServletMappingHandler(servletClass, mappingUrl, method);
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
        INIT_STATE_MAP.put(servletClass, true);
    }

    public final Map<AbstractRestServlet, MRestServlet> servletHandlerMap = new WeakHashMap<>();

    private volatile boolean initialized = false;
    public synchronized void init() {
        if (initialized) {
            return;
        }
        init0();
        initialized = true;
    }

    private MRestServlet getServletHandlerInstance(Class<? extends MRestServlet> servletHandlerClass) {
        MRestServlet servletInstance = servletHandlerMap.get(this);
        if (servletInstance == null) {
            synchronized (servletHandlerMap) {
                final MRestServlet servletInstance0 = servletHandlerMap.get(this);
                if (servletInstance0 == null) {
                    try {
                        servletInstance = servletHandlerClass.getConstructor(this.getClass()).newInstance(this);
                    } catch (Throwable throwable) {
                        throw new MRestHandleException(String.format("create servlet mapping handler instance failed, class: %s", servletHandlerClass.getName()), throwable);
                    }
                } else {
                    servletInstance = servletInstance0;
                }
            }
        }
        return servletInstance;
    }

    public List<String> getMappingUrlList() {
        init();
        return new ArrayList<>(MAPPING_CLASS_MAP.keySet());
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        init();
        String requestUrl = restRequest.getUrl();
        // TODO 根据url匹配请求处理ServletMappingClass对象
        List<String> urlList = getMappingUrlList();
        String matchedUrl = null;
        for (String _patternUrl: urlList) {
            UrlModel urlModel = new UrlModel(requestUrl);
            UrlPatternModel urlPatternModel = new UrlPatternModel(_patternUrl);
            String urlPattern = urlPatternModel.getUrlPattern();
            if (urlPatternModel.isPatternExt()) {
                String pattern = "^" + urlPattern.replace("*", "\\S+") + "$";
                if (requestUrl.matches(pattern)) {
                    matchedUrl = _patternUrl;
                    break;
                }
            }
            if (urlPatternModel.isPatternPathMatch()) {
                String pattern = "^" + urlPattern.replace("*", "\\S*") + "$";
                if (requestUrl.matches(pattern)) {
                    break;
                }
            }
            if (urlPatternModel.isPatternStrictly()) {
                if (urlPatternModel.isSupportPlaceholder()) {
                    List<UrlPathModel> urlPathModelList = urlModel.getPathModelList();
                    List<UrlPatternPathModel> urlPatternPathModelList = urlPatternModel.getPatternPathModelList();
                    int pathModelListSize = urlModel.getPathModelListSize();
                    int patternPathModelListSize = urlPatternModel.getPatternPathModelListSize();
                    if (pathModelListSize == patternPathModelListSize) {
                        boolean match = true;
                        for (int index = 0; index < pathModelListSize; index++) {
                            UrlPathModel pathModel = urlPathModelList.get(index);
                            UrlPatternPathModel patternPathModel = urlPatternPathModelList.get(index);
                            if (patternPathModel.isPlaceholder()) {
                                // do nothing.
                            } else if (!patternPathModel.getPathVal().equals(pathModel.getPathVal())) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            matchedUrl = _patternUrl;
                            break;
                        }
                    }
                } else if (requestUrl.equals(urlPattern)) {
                    matchedUrl = _patternUrl;
                    break;
                }
            }
        }
        ServletMappingClass mappingClass = MAPPING_CLASS_MAP.get(matchedUrl);
        if (mappingClass == null) {
            restResponse.writeStatusPageAsHtml(HttpResponseStatus.NOT_FOUND);
            return;
        }
        ServletMappingHandler mappingHandler = mappingClass.getMappingHandler(matchedUrl);
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
