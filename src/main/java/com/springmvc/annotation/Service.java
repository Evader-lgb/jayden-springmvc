package com.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @Description: 自定义注解
 */
@Target(ElementType.TYPE)  //元注解
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {

    String value();
}
