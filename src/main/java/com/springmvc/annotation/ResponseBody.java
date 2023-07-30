package com.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @BelongsProject: SpringMvc
 * @Description: TODO
 */
@Target(ElementType.METHOD)  //元注解
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
