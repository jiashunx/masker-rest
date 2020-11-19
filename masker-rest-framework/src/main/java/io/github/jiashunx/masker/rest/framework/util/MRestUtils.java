package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 工具类
 * @author jiashunx
 */
public class MRestUtils {

    private static final Logger logger = LoggerFactory.getLogger(MRestUtils.class);
    private static final String frameworkName;
    private static final String frameworkVersion;
    static {
        Properties properties = new Properties();
        try {
            properties.load(MRestUtils.class.getResourceAsStream("/META-INF/maven/io.github.jiashunx/masker-rest-framework/pom.properties"));
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("get bundle version failed.", throwable);
            }
            properties.put("version", "test");
        }
        frameworkName = "masker-rest";
        frameworkVersion = properties.getProperty("version");
    }

    public static int getDefaultServerPort() {
        return Constants.DEFAULT_SERVER_PORT;
    }

    public static String getDefaultServerName() {
        return Constants.DEFAULT_SERVER_NAME;
    }

    public static String getFrameworkName() {
        return frameworkName;
    }

    public static String getFrameworkVersion() {
        return frameworkVersion;
    }

}
