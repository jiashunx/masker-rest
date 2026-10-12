package io.github.jiashunx.masker.rest.framework.util;

import org.junit.Assert;
import org.junit.Test;

import io.github.jiashunx.masker.rest.framework.model.MRestServerConfig;

/**
 * @author jiashunx
 */
public class MRestUtilsTest {

    @Test
    public void test_format() {
        String content = MRestUtils.format("123#{key}, hello, $#{key}$", "key", "myvalue");
        Assert.assertNotNull(content);
    }

    @Test
    public void test_getDefaultServerConfig_returnsCopy() {
        MRestServerConfig config0 = MRestUtils.getDefaultServerConfig();
        String name0 = config0.getServerName();
        // 修改返回的副本不应影响全局默认配置
        config0.setServerName("modified-by-test");
        Assert.assertEquals(name0, MRestUtils.getDefaultServerConfig().getServerName());
    }

}
