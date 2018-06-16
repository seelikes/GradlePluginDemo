package com.github.seelikes.gradle.plugin.demo;

import com.github.seelikes.groovy.plugin.ao.annotation.Inject;

public class MKL {
    @Inject("APPLICATION_ID")
    public static final String DN = "DN";

    @Inject("BUILD_TYPE")
    public static final String DBT = "BUILD_TYPE";

    @Inject("DEBUG")
    public static final boolean DB = false;
}
