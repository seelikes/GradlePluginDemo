package com.github.seelikes.groovy.plugin.ao

import com.github.seelikes.groovy.plugin.ao.annotation.Inject
import com.google.gson.Gson
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.Loader
import javassist.bytecode.AttributeInfo
import javassist.bytecode.ConstantAttribute
import org.gradle.api.Project

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class AOInject {
    static BuildConfigHolder holder
    static CtClass StringClass

    static void inject(String path, Project project) {
        ClassPool.default.appendClassPath path
        ClassPool.default.appendClassPath project.android.bootClasspath[0].toString()
        ClassPool.default.importPackage "android.os.Bundle"

        File dir = new File(path)
        if (dir.directory) {
            dir.eachFileRecurse { file ->
                if (file.name.endsWith(".class")) {

                    if (file.name.matches(/^R\$[a-zA-Z]+?\.class$/) || file.name == "R.class") {
                        return
                    }

                    String filePath = file.absolutePath
                    println "filePath: " + filePath

                    if (holder == null) {
                        holder = new BuildConfigHolder(project.extensions.ao.packageName + "." + "BuildConfig")
                    }

                    if (StringClass == null) {
                        StringClass = ClassPool.default.get(String.class.canonicalName)
                    }

                    def classCanonicalName = findClassName(filePath, project)
                    if (classCanonicalName == null || classCanonicalName.empty) {
                        return
                    }
                    if (classCanonicalName == project.extensions.ao.packageName + "." + "BuildConfig.class") {
                        return
                    }


                    CtClass ctClass = ClassPool.default.getCtClass(classCanonicalName.substring(0, classCanonicalName.length() - 6))
                    if (ctClass.frozen) {
                        ctClass.defrost()
                    }

                    for (CtField field : ctClass.declaredFields) {
                        if ((field.modifiers & Modifier.STATIC) == Modifier.STATIC) {
                            Inject inject = field.getAnnotation(Inject.class)
                            println "name: " + field.name + "; inject == null: " + (inject == null)
                            if (inject == null) {
                                continue
                            }
                            def value = holder.getValueAsString(inject.value())
                            println "name: " + field.name + "; value != null: " + (value != null)
                            if (value != null) {
                                ctClass.removeField(field)
                                println "name: " + field.name + "; value instanceof CtField.Initializer: " + (value instanceof CtField.Initializer)
                                println "name: " + field.name + "; value.class.canonicalName: " + value.class.canonicalName
                                if (value instanceof CtField.Initializer) {
                                    ctClass.addField(field, value)
                                }
                                else if (field.type == StringClass) {
                                    ctClass.addField(field, "\"" + value + "\"")
                                }
                                else if (field.type == CtClass.charType) {
                                    ctClass.addField(field, "\'" + value + "\'")
                                }
                                else {
                                    ctClass.addField(field, value)
                                }
                            }
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
        Class<?> javaClassBuildConfig
        Map<String, CtField> fields

        BuildConfigHolder(String canonicalName) {
            ClassPool.default.appendClassPath canonicalName
            Loader loader = new Loader(ClassPool.default)
            this.javaClassBuildConfig = loader.loadClass(canonicalName)
            println "VV"
            this.BuildConfig = ClassPool.default.getCtClass(canonicalName)
            println "AA"
            if (this.BuildConfig.frozen) {
                this.BuildConfig.defrost()
            }
            for (AttributeInfo attribute : BuildConfig.classFile.attributes) {
                println "attribute.name: " + attribute.name
            }
            fields = new HashMap<>()
            for (CtField field : this.BuildConfig.declaredFields) {
                if ((field.modifiers & Modifier.STATIC) == Modifier.STATIC && (field.modifiers & Modifier.PUBLIC) == Modifier.PUBLIC) {
                    println "name: " + field.name  + "; field.fieldInfo.descriptor: " + field.fieldInfo.descriptor
                    fields.put(field.name, field)
                    for (AttributeInfo attribute : field.fieldInfo.attributes) {
                        println "name: " + field.name + "; attribute.name: " + attribute.name
                    }
                }
            }

            for (MetaProperty property : BuildConfig.classFile.metaClass.properties) {
                println "property.name: " + property.name
            }
        }

        String getValueAsString(String name) {
            CtField field = fields.get(name)
            println "getValueAsString, name: " + name + "; field == null: " + (field == null)
            if (field == null) {
                return "null"
            }
            println "name: " + name + "; field.constantValue: " + field.constantValue

            if (field.constantValue != null) {
                return String.valueOf(field.constantValue)
            }

            Method getInit = field.class.getDeclaredMethod "getInit"
            println "name: " + name + "; getInit != null: " + (getInit != null)
            if (getInit != null) {
                boolean accessible = getInit.isAccessible()
                try {
                    getInit.setAccessible true
                    CtField.Initializer initializer = getInit.invoke(field)
                    if (initializer != null) {
                        return initializer
                    }
                }
                finally {
                    getInit.setAccessible(accessible)
                }
            }

            println "name: " + name + "; field.fieldInfo.constantValue: " + field.fieldInfo.constantValue

            println "name: " + name + "; field.fieldInfo2.constantValue: " + field.fieldInfo2.constantValue

            return javaClassBuildConfig.getDeclaredField(name).get(null)
        }
    }
}
