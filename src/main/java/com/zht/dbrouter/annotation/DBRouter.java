package com.zht.dbrouter.annotation;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE ,ElementType.METHOD})
public @interface DBRouter {
    String key() default "";
}
