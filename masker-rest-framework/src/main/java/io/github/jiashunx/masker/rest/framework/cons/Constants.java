package io.github.jiashunx.masker.rest.framework.cons;

/**
 * 常量类
 * @author jiashunx
 */
public class Constants {
    /**
     * 默认的http请求报文字节大小限制
     */
    public static final int HTTP_CONTENT_MAX_BYTE_SIZE = 50*1024*1024;

    public static final Character CHAR_PATH_SEP = '/';
    public static final String PATH_SEP = "/";
    public static final String ROOT_PATH = "/";
    public static final String PATH_SEP_WIN = "\\";
    public static final String INDEX_PATH = "/index.html";
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final String DEFAULT_WEBSOCKET_CONTEXT_PATH = "/websocket";
    public static final int DEFAULT_FILTER_ORDER = 0;
    public static final String DEFAULT_FILTER_URLPATTERN = "/*";
    public static final String[] DEFAULT_FILTER_URLPATTERNS = new String[] { DEFAULT_FILTER_URLPATTERN };
    public static final String DEFAULT_SERVLET_URLPATTERN = "/*";
    public static final String PATH_MATCH_ALL = "/*";
    public static final String STRING_MATCH_ALL = "*";
    public static final String PATH_MATCH_ALL_PREFIX = "*.";
    public static final String REGEX_PREFIX = "^";
    public static final String REGEX_SUFFIX = "$";
    public static final String REGEX_CHAR0N = "\\S*";
    public static final String REGEX_CHAR1N = "\\S+";

    public static final String HTTP_HEADER_ACCEPT = "Accept";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HTTP_HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HTTP_HEADER_CONNECTION = "Connection";
    public static final String HTTP_HEADER_LOCATION = "Location";
    public static final String HTTP_HEADER_COOKIE = "Cookie";
    public static final String HTTP_HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HTTP_HEADER_UPGRADE = "Upgrade";
    public static final String HTTP_HEADER_SERVER_FRAMEWORK_NAME = "Server-Name";
    public static final String HTTP_HEADER_SERVER_FRAMEWORK_VERSION = "Server-Version";
    public static final String HTTP_HEADER_SERVER_STARTUP_TIME = "Server-Startup-Time";
    public static final String HTTP_HEADER_SERVER_IDENTIFIER = "Server-Identifier";

    public static final String CONTENT_TYPE_MULTIPART_FORMDATA = "multipart/form-data";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    public static final String CONTENT_TYPE_APPLICATION_OCTETSTREAM = "application/octet-stream";
    public static final String CONNECTION_KEEP_ALIVE = "keep-alive";
    public static final String UPGRADE_WEBSOCKET = "websocket";

}
