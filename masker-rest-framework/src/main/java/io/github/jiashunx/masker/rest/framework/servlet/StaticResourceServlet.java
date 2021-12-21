package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class StaticResourceServlet implements MRestServlet {

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
        // 默认请求url处理
        if (Constants.ROOT_PATH.equals(requestUrl) || Constants.INDEX_PATH.equals(requestUrl)) {
            if (Constants.ROOT_PATH.equals(requestUrl)) {
                // 指定了index url, 服务端进行重定向
                String indexUrl = restContext.getIndexUrl();
                if (StringUtils.isNotEmpty(indexUrl) && !Constants.ROOT_PATH.equals(indexUrl)) {
                    restResponse.redirect(indexUrl);
                    return;
                }
                restResponse.redirect(Constants.INDEX_PATH);
            }
            if (Constants.INDEX_PATH.equals(requestUrl)) {
                // 静态资源指定了index url
                StaticResource indexResource = restContext.getStaticResourceFinder().loadResource(Constants.INDEX_PATH);
                if (indexResource != null) {
                    restResponse.write(indexResource.getContentBytes(), MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, indexResource.getContentType()));
                    return;
                }
            }
            // 输出默认masker-rest主页面
            restResponse.write(DEFAULT_PAGE_BYTES, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_HTML));
            return;
        }
        // TODO 静态资源匹配, 请求url与注册的classpath|diskpath静态资源进行匹配, 然后根据请求url进行遍历查找, 同时获取文件Content-Type
        StaticResource staticResource = restContext.getStaticResourceFinder().loadResource(requestUrl);
        if (staticResource != null) {
            restResponse.write(staticResource.getContentBytes(), MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, staticResource.getContentType()));
            return;
        }
    }

}
