package io.github.jiashunx.masker.rest.framework.servlet.mapping;

import java.lang.annotation.*;

/**
 * @author jiashunx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface PostMapping {

    String url();

}
