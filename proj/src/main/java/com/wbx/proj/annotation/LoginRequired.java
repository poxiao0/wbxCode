package com.wbx.proj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 作用目标
@Target(ElementType.METHOD)
// 作用范围
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
}
