package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.UrlModel;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class UrlParaserTest {

    @Test
    public void test_parseRequestUrl() {
        UrlModel urlModel = UrlParaser.parseRequestUrl("/");
        urlModel = UrlParaser.parseRequestUrl("/abc");
        urlModel = UrlParaser.parseRequestUrl("/abc/");
        urlModel = UrlParaser.parseRequestUrl("/abc/abcd");
        urlModel = UrlParaser.parseRequestUrl("/abc/abcd/");
        System.out.println(urlModel);
    }

}
