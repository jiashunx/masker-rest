package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.model.UrlModel;
import io.github.jiashunx.masker.rest.framework.model.UrlPatternModel;
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
        UrlPatternModel urlPatternModel0 = new UrlPatternModel("/");
        UrlPatternModel urlPatternModel1 = new UrlPatternModel("/abc");
        UrlPatternModel urlPatternModel2 = new UrlPatternModel("/abc/");
        UrlPatternModel urlPatternModel3 = new UrlPatternModel("/abcd/*");
        UrlPatternModel urlPatternModel4 = new UrlPatternModel("/*");
        UrlPatternModel urlPatternModel5 = new UrlPatternModel("*.do");
        UrlPatternModel urlPatternModel6 = new UrlPatternModel("/abcd/{ac}/{abc}");
        System.out.println("");
    }

}
