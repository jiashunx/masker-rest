package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.cons.Constants;

import java.lang.annotation.*;

/**
 * @author jiashunx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface MFilter {

    /**
     * filter顺序, 实际执行时filter执行顺序按此值大小从小到大顺序执行.
     * @return int
     */
    int order() default Constants.DEFAULT_FILTER_ORDER;

    /**
     * url匹配列表
     * @return String[]
     */
    String[] urlPatterns() default { Constants.DEFAULT_FILTER_URLPATTERN };

}
