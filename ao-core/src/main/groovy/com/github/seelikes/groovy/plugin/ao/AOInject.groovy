package com.github.seelikes.groovy.plugin.ao

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

class AOInject {
    static CtClass BuildConfig;

    static void inject(String path, Project project) {
        ClassPool.default.appendClassPath path
        ClassPool.default.appendClassPath project.android.bootClasspath[0].toString()
        ClassPool.default.importPackage "android.os.Bundle"

        File dir = new File(path)
        if (dir.directory) {
            dir.eachFileRecurse { file ->
                String filePath = file.absolutePath
                println "filePath: " + filePath
                println "project.buildDir.absolutePath: " + project.buildDir
                println "project.extensions.ao.packageName: " + project.extensions.ao.packageName

                if (BuildConfig == null) {
                    ClassPool.default.importPackage project.extensions.ao.packageName + "." + "BuildConfig"
                    BuildConfig = ClassPool.default.getCtClass(project.extensions.ao.packageName + "." + "BuildConfig")
                }

                if (file.name == "MainActivity.class") {
                    CtClass ctClass = ClassPool.default.getCtClass(findClassName(filePath, project))
                    println "ctClass = " + ctClass
                    if (ctClass.frozen) {
                        ctClass.defrost()
                    }

                    CtMethod ctMethod = ctClass.getDeclaredMethod("onCreate")

                    println "方法名 = " + ctMethod

                    String insetBeforeStr = """ android.widget.Toast.makeText(this,"我是被插入的Toast代码~!!",android.widget.Toast.LENGTH_SHORT).show();
                                                """
                    ctMethod.insertBefore insetBeforeStr
                    ctClass.writeFile path
                    ctClass.detach()
                }
            }
        }
    }

    static String findClassName(String path, Project project) {
        String packagePath = project.extensions.ao.packageName.replaceAll("\\.", "\\\\")
        println "packagePath: " + packagePath
        println "path: " + path
        println "path.indexOf(packagePath): " + path.indexOf(packagePath)
        return path.substring(path.indexOf(packagePath)).replaceAll("\\\\", ".")
    }
}
