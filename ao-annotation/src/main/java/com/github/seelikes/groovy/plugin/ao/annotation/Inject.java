package com.github.seelikes.groovy.plugin.ao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Inject {
    /**
     * 指定该项从BuildConfig中承接的属性的名字
     * @return 从BuildConfig中承接的属性的名字
     */
    String value();
}
