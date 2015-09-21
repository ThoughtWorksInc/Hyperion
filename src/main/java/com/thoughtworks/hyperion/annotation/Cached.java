package com.thoughtworks.hyperion.annotation;


import com.thoughtworks.hyperion.enumeration.CacheScope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    String key();
    CacheScope scope() default CacheScope.CLASS;
    int expireMinutes() default 10;
}
