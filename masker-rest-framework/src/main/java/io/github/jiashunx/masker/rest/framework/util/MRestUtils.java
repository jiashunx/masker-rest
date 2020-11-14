package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;

/**
 * 工具类
 * @author jiashunx
 */
public class MRestUtils {

    /**
     * 获取默认server端口
     * @return int
     */
    public static int getDefaultServerPort() {
        return Constants.DEFAULT_SERVER_PORT;
    }

    /**
     * 获取默认server名称
     * @return String
     */
    public static String getDefaultServerName() {
        return Constants.DEFAULT_SERVER_NAME;
    }

}
