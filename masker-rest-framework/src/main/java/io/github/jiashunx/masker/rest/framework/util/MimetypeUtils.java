package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiashunx
 */
public class MimetypeUtils {

    private static final Logger logger = LoggerFactory.getLogger(MimetypeUtils.class);

    public static final String DEFAULT_CONTENT_TYPE_KEY = ".*";
    public static final String DEFAULT_CONTENT_TYPE_VALUE = "application/octet-stream";

    /**
     * 共享的MimetypesFileTypeMap实例(构造时会加载MIME配置, 避免每次查找都重新构造).
     */
    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();

    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();
    static {
        try {
            String json = IOUtils.loadContentFromClasspath("masker-rest/content-type.json");
            Map<?, ?> map = MRestSerializer.jsonToObj(json, Map.class);
            map.forEach((key, value) -> {
                CONTENT_TYPE_MAP.put(String.valueOf(key), String.valueOf(value));
            });
        } catch (Throwable throwable) {
            logger.error("load content-type.json failed", throwable);
        }
    }

    /**
     * 根据文件名获取MIME类型(使用共享MimetypesFileTypeMap实例).
     * @param fileName 文件名
     * @return MIME类型
     */
    public static String getMimetypeContentType(String fileName) {
        return MIMETYPES_FILE_TYPE_MAP.getContentType(fileName);
    }

    public static String getResourceContentType(String fileName) {
        try {
            String suffix = DEFAULT_CONTENT_TYPE_KEY;
            int index = fileName.lastIndexOf(".");
            if (index > 0) {
                suffix = fileName.substring(index);
            }
            String contentType = CONTENT_TYPE_MAP.get(suffix);
            if (contentType == null || contentType.equals(DEFAULT_CONTENT_TYPE_VALUE)) {
                contentType = MIMETYPES_FILE_TYPE_MAP.getContentType(fileName);
            }
            if (StringUtils.isEmpty(contentType)) {
                contentType = DEFAULT_CONTENT_TYPE_VALUE;
            }
            return contentType;
        } catch (Throwable throwable) {
            logger.error("get resource content-type failed, fileName=" + fileName, throwable);
        }
        return DEFAULT_CONTENT_TYPE_VALUE;
    }

}
