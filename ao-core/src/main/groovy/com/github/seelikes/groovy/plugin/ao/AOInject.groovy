package com.github.seelikes.groovy.plugin.ao

import com.github.seelikes.groovy.plugin.ao.annotation.Inject
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import org.gradle.api.Project

import java.lang.reflect.Modifier

class AOInject {
    static BuildConfigHolder holder
    static CtClass StringClass
    static CtClass CharacterClass
    static CtClass charClass

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
                        ClassPool.default.appendClassPath project.extensions.ao.packageName + "." + "BuildConfig"
                        CtClass BuildConfig = ClassPool.default.getCtClass(project.extensions.ao.packageName + "." + "BuildConfig")
                        holder = new BuildConfigHolder(BuildConfig)
                    }

                    if (StringClass == null) {
                        StringClass = ClassPool.default.get(String.class.canonicalName)
                    }

                    if (CharacterClass == null) {
                        CharacterClass = ClassPool.default.get(Character.class.canonicalName)
                    }

                    if (charClass == null) {
                        charClass = ClassPool.default.get(char.class.canonicalName)
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
                            ctClass.removeField(field)
                            if (field.type == StringClass) {
                                ctClass.addField(field, "\"" + holder.getValueAsString(inject.value()) + "\"")
                            }
                            else if (field.type == CharacterClass || field.type == charClass) {
                                ctClass.addField(field, "\'" + holder.getValueAsString(inject.value()) + "\'")
                            }
                            else {
                                ctClass.addField(field, holder.getValueAsString(inject.value()))
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
            println "getValueAsString, name: " + name + "; field == null: " + (field == null)
            if (field == null) {
                return "null"
            }
            println "name: " + name + "; field.constantValue: " + field.constantValue

            if (field.constantValue != null) {
                return String.valueOf(field.constantValue)
            }

        }
    }
}
