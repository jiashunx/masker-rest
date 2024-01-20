package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class StaticResourceServlet implements MRestServlet {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceServlet.class);

    private static byte[] DEFAULT_PAGE_BYTES = null;
    static {
        String template = IOUtils.loadContentFromClasspath("masker-rest/template/index.html", MRestDispatchServlet.class.getClassLoader());
        DEFAULT_PAGE_BYTES = MRestUtils.format(template, "mrf.version", MRestUtils.getFrameworkVersion()).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void service(MRestRequest restRequest, MRestResponse restResponse) {
        if (restResponse.isWriteMethodInvoked()) {
            return;
        }
        String requestUrl = restRequest.getUrl();
        MRestContext restContext = restRequest.getRestContext();
        // context的重定向index-url
        String redirectUrl = Constants.INDEX_PATH;
        // 指定了index-url, 替换默认index-url
        String indexUrl = restContext.getIndexUrl();
        if (StringUtils.isNotEmpty(indexUrl) && !Constants.ROOT_PATH.equals(indexUrl)) {
            redirectUrl = indexUrl;
        }
        // 默认请求url处理
        if (Constants.ROOT_PATH.equals(requestUrl)) {
            if (restRequest.getMethod() == HttpMethod.GET) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} redirect root url: [{}] -> [{}]", restContext.getContextDesc(), requestUrl, redirectUrl);
                }
                restResponse.redirect(redirectUrl);
            }
            return;
        }
        // 静态资源指定了index-url
        StaticResource indexResource = restContext.getStaticResourceFinder().loadResource(requestUrl);
        if (indexResource != null) {
            writeStaticResource(restRequest, restResponse, indexResource);
            return;
        }
        // 自定义index-url或默认index-url未找到静态资源
        if (redirectUrl.equals(requestUrl) || Constants.INDEX_PATH.equals(requestUrl)) {
            // GET请求输出默认masker-rest主页面
            if (restRequest.getMethod() == HttpMethod.GET) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} locate classpath resource: [{}], output default index", restContext.getContextDesc(), requestUrl);
                }
                writeStaticResource(restRequest, restResponse, DEFAULT_PAGE_BYTES, Constants.CONTENT_TYPE_TEXT_HTML);
            }
        }
    }

    private void writeStaticResource(MRestRequest restRequest, MRestResponse restResponse, StaticResource staticResource) {
        writeStaticResource(restRequest, restResponse, staticResource.getContentBytes(), staticResource.getContentType());
    }

    private void writeStaticResource(MRestRequest restRequest, MRestResponse restResponse, byte[] bytes, String contentType) {
        if (restRequest.getMethod() == HttpMethod.GET) {
            restResponse.setHeader(Constants.HTTP_HEADER_CONTENT_TYPE, contentType);
            restResponse.write(bytes);
        } else {
            restResponse.write(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

}
