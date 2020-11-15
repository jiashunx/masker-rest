package io.github.jiashunx.masker.rest.framework.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class MRestJWTHelperTest {

    @Test
    public void test() {
        System.out.println(System.currentTimeMillis());
        MRestJWTHelper jwtHelper = new MRestJWTHelper("1234567890");
        String jwtToken = jwtHelper.newToken(MRestHeaderBuilder.Build("mk", "mv"), MRestHeaderBuilder.Build("nk", "nv"));
        Assert.assertNotNull(jwtToken);
        Assert.assertTrue(jwtHelper.isTokenValid(jwtToken));
        Assert.assertNotNull(jwtHelper.updateToken(jwtToken));
    }

}
