package com.github.seelikes.gradle.plugin.demo;

import com.github.seelikes.groovy.plugin.ao.annotation.Inject;

public class MKL {
    @Inject("APPLICATION_ID")
    public static String DN = "DN";

    @Inject("BUILD_TYPE")
    public static String DBT = "BUILD_TYPE";

    @Inject("DEBUG")
    public static boolean DB = false;

    @Inject("HHHH")
    public static boolean HHHH = true;

    public MKL() {
        if (HHHH) {
            System.out.println("HHHH");
        }
        if ("DN".equals(DN)) {
            System.out.println("DN");
        }
    }
}
