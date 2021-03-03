package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.UrlModel;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class UrlParaserTest {

    @Test
    public void test_parseRequestUrl() {
        UrlModel urlModel0 = new UrlModel("/");
        UrlModel urlModel1 = new UrlModel("/abc");
        UrlModel urlModel2 = new UrlModel("/abc/");
        UrlModel urlModel3 = new UrlModel("/abc/abcd");
        UrlModel urlModel4 = new UrlModel("/abc/abcd/");
        System.out.println("");
    }

}
