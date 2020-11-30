package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

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
            properties.load(MRestUtils.class.getResourceAsStream("/META-INF/maven/io.github.jiashunx/masker-rest-framework/pom.properties"));
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

    public static String getSystemTempDirPath() {
        return systemTempDirPath;
    }

    public static String getUserDirPath() {
        return userDirPath;
    }

}
