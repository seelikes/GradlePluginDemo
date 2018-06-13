package com.github.seelikes.groovy.plugin.ao

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import org.gradle.api.Project

import java.lang.reflect.Modifier

class AOInject {
    static BuildConfigHolder holder

    static void inject(String path, Project project) {
        ClassPool.default.appendClassPath path
        ClassPool.default.appendClassPath project.android.bootClasspath[0].toString()
        ClassPool.default.importPackage "android.os.Bundle"

        File dir = new File(path)
        if (dir.directory) {
            dir.eachFileRecurse { file ->
                if (file.name.endsWith(".class")) {
                    String filePath = file.absolutePath
                    println "filePath: " + filePath
                    println "project.buildDir.absolutePath: " + project.buildDir
                    println "project.extensions.ao.packageName: " + project.extensions.ao.packageName

                    if (holder == null) {
                        ClassPool.default.appendClassPath project.extensions.ao.packageName + "." + "BuildConfig"
                        CtClass BuildConfig = ClassPool.default.getCtClass(project.extensions.ao.packageName + "." + "BuildConfig")
                        holder = new BuildConfigHolder(BuildConfig)
                    }

                    def className = findClassName(filePath, project)
                    println "hxhxxhxhxxh"
                    if (className == null || className.empty) {
                        return
                    }
                    println "zdvf sdgsgrrdfg"
                    if (className == project.extensions.ao.packageName + "." + "BuildConfig.class") {
                        return
                    }
                    println "className: " + className
                    CtClass ctClass = ClassPool.default.getCtClass(className.substring(0, className.length() - 6))
                    println "ctClass: " + ctClass
                    if (ctClass.frozen) {
                        ctClass.defrost()
                    }

                    for (CtField field : ctClass.declaredFields) {
                        println "(field.modifiers & Modifier.STATIC) == Modifier.STATIC: " + ((field.modifiers & Modifier.STATIC) == Modifier.STATIC)
                        if ((field.modifiers & Modifier.STATIC) == Modifier.STATIC) {
                            ctClass.removeField(field)
                            ctClass.addField(field, holder.getValueAsString(field.name))
                        }
                    }

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
        if (path.indexOf(packagePath) == -1) {
            return null
        }
        return path.substring(path.indexOf(packagePath)).replaceAll("\\\\", ".")
    }

    static class BuildConfigHolder {
        CtClass BuildConfig
        Map<String, CtField> fields

        BuildConfigHolder(CtClass BuildConfig) {
            this.BuildConfig = BuildConfig
            fields = new HashMap<>()
            for (CtField field : this.BuildConfig.declaredFields) {
                if ((field.modifiers & Modifier.STATIC) == Modifier.STATIC && (field.modifiers & Modifier.PUBLIC) == Modifier.PUBLIC) {
                    fields.put(field.name, field)
                }
            }
        }

        String getValueAsString(String name) {
            CtField field = fields.get(name)
            if (field == null) {
                return "null"
            }
            return String.valueOf(field.getConstantValue())
        }
    }
}
