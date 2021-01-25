package io.github.jiashunx.masker.rest.framework.servlet.mapping;

import io.github.jiashunx.masker.rest.framework.cons.Constants;

import java.lang.annotation.*;

/**
 * @author jiashunx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
public @interface RequestMapping {

    String url() default Constants.ROOT_PATH;

    HttpMethod[] method() default {
            HttpMethod.OPTIONS,
            HttpMethod.GET,
            HttpMethod.HEAD,
            HttpMethod.POST,
            HttpMethod.PUT,
            HttpMethod.PATCH,
            HttpMethod.DELETE,
            HttpMethod.TRACE,
            HttpMethod.CONNECT
    };

}
