package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.function.VoidFunc;
import io.github.jiashunx.masker.rest.framework.model.MRestServerConfig;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * 工具类
 * @author jiashunx
 */
public class MRestUtils {

    private static final Logger logger = LoggerFactory.getLogger(MRestUtils.class);
    private static final String frameworkName;
    private static final String frameworkVersion;
    private static String systemTempDirPath;
    private static String userDirPath;
    static {
        Properties properties = new Properties();
        try {
            properties.putAll(IOUtils.loadPropertiesFromClasspath("META-INF/maven/io.github.jiashunx/masker-rest-framework/pom.properties"));
            if (properties.isEmpty()) {
                throw new NullPointerException();
            }
        } catch (Throwable throwable) {
            if (logger.isWarnEnabled()) {
                logger.warn("get bundle version failed, error reason: {}", throwable.getMessage());
                logger.warn("reset bundle version to: test");
            }
            properties.put("version", "test");
        }
        frameworkName = "masker-rest";
        frameworkVersion = properties.getProperty("version");
        // windows: C:\Users\JIASHUNX~1\AppData\Local\Temp\
        // linux: /tmp
        systemTempDirPath = System.getProperty("java.io.tmpdir");
        if (!systemTempDirPath.endsWith(File.separator)) {
            systemTempDirPath = systemTempDirPath + File.separator;
        }
        userDirPath = System.getProperty("user.dir");
        if (!userDirPath.endsWith(File.separator)) {
            userDirPath = userDirPath + File.separator;
        }
    }

    public static int getDefaultServerPort() {
        return getDefaultServerConfig().getServerPort();
    }

    public static String getDefaultServerName() {
        return getDefaultServerConfig().getServerName();
    }

    public static String getFrameworkName() {
        return frameworkName;
    }

    public static String getFrameworkVersion() {
        return frameworkVersion;
    }

    public static String getSystemTempDirPath() {
        return systemTempDirPath;
    }

    public static String getUserDirPath() {
        return userDirPath;
    }

    private static volatile MRestServerConfig serverDefaultConfig;

    public static MRestServerConfig getDefaultServerConfig() {
        if (serverDefaultConfig == null) {
            synchronized (MRestUtils.class) {
                if (serverDefaultConfig == null) {
                    serverDefaultConfig = MRestSerializer.jsonToObj(
                            IOUtils.loadBytesFromClasspath("masker-rest/default-config.json")
                            , MRestServerConfig.class);
                }
            }
        }
        return serverDefaultConfig;
    }

    public static String formatContextPath(String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            throw new IllegalArgumentException("contextPath can't be empty");
        }
        String _ctxPath = contextPath.trim();
        while (_ctxPath.endsWith(Constants.URL_PATH_SEP)) {
            if (_ctxPath.length() == 1) {
                break;
            }
            _ctxPath = _ctxPath.substring(0, _ctxPath.length() - 1);
        }
        if (!_ctxPath.startsWith(Constants.URL_PATH_SEP)) {
            _ctxPath = Constants.URL_PATH_SEP;
        }
        return _ctxPath;
    }

    public static void tryCatch(VoidFunc voidFunc, Consumer<Throwable> consumer) {
        try {
            voidFunc.doSomething();
        } catch (Throwable throwable) {
            consumer.accept(throwable);
        }
    }

    public static String format(String template, String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return format(template, params);
    }

    public static String format(String template, Map<String, Object> params) {
        String content = String.valueOf(template);
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry: params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                content = content.replace("#{" + key + "}", String.valueOf(entry.getValue()));
            }
        }
        return content;
    }

}
