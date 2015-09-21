package com.thoughtworks.hyperion.annotation;

import com.thoughtworks.hyperion.enumeration.NoCacheCondition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface NoCacheIf {
    NoCacheCondition condition();
}
