package io.github.jiashunx.masker.rest.framework.cons;

/**
 * 常量类
 * @author jiashunx
 */
public class Constants {

    public static final int HTTP_CONTENT_MAX_LENGTH = 50*1024*1024;
    public static final String ROOT_PATH = "/";
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final String DEFAULT_WEBSOCKET_CONTEXT_PATH = "/websocket";
    public static final String URL_PATH_SEP = "/";
    public static final int DEFAULT_FILTER_ORDER = 0;
    public static final String DEFAULT_FILTER_URLPATTERN = "/*";
    public static final String[] DEFAULT_FILTER_URLPATTERNS = new String[] { DEFAULT_FILTER_URLPATTERN };

    public static final String HTTP_HEADER_ACCEPT = "Accept";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HTTP_HEADER_CONNECTION = "Connection";
    public static final String HTTP_HEADER_LOCATION = "Location";
    public static final String HTTP_HEADER_COOKIE = "Cookie";
    public static final String HTTP_HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HTTP_HEADER_UPGRADE = "Upgrade";
    public static final String HTTP_HEADER_SERVER_FRAMEWORK_NAME = "Server-Framework-Name";
    public static final String HTTP_HEADER_SERVER_FRAMEWORK_VERSION = "Server-Framework-Version";

    public static final String CONTENT_TYPE_MULTIPART_FORMDATA = "multipart/form-data";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CONTENT_TYPE_APPLICATION_OCTETSTREAM = "application/octet-stream";
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    public static final String UPGRADE_WEBSOCKET = "websocket";

}
