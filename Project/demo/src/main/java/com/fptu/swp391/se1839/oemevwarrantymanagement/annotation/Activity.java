package com.fptu.swp391.se1839.oemevwarrantymanagement.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Activity {
    String title();

    String status() default "INFO";

    String detail() default "";
}