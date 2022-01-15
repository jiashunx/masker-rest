package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.cons.Constants;

import java.lang.annotation.*;

/**
 * @author jiashunx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface MServlet {

    /**
     * url匹配字符串
     * @return String
     */
    String urlPattern() default Constants.DEFAULT_SERVLET_URLPATTERN;

}
