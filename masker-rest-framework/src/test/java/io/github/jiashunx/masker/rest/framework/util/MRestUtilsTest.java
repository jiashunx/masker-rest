package io.github.jiashunx.masker.rest.framework.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class MRestUtilsTest {

    @Test
    public void test_format() {
        String content = MRestUtils.format("123#{key}, hello, $#{key}$", "key", "myvalue");
        Assert.assertNotNull(content);
    }

}
