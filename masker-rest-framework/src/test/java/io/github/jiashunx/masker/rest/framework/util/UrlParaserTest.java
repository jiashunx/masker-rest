package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.UrlModel;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class UrlParaserTest {

    @Test
    public void test_parseRequestUrl() {
        UrlModel urlModel0 = UrlParaser.parseRequestUrl("/");
        UrlModel urlModel1 = UrlParaser.parseRequestUrl("/abc");
        UrlModel urlModel2 = UrlParaser.parseRequestUrl("/abc/");
        UrlModel urlModel3 = UrlParaser.parseRequestUrl("/abc/abcd");
        UrlModel urlModel4 = UrlParaser.parseRequestUrl("/abc/abcd/");
        System.out.println("");
    }

}
