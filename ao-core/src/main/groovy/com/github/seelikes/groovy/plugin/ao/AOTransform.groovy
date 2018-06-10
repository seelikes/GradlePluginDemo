package com.github.seelikes.groovy.plugin.ao

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class AOTransform extends Transform {
    Project project

    AOTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "AO"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println "AOTransform start..."

        if (project.extensions.ao.packageName == null || project.extensions.ao.packageName.empty) {
            project.extensions.ao.packageName = project.extensions.getByName(AppExtension).defaultConfig.applicationId
        }

        println "project.extensions.ao.packageName: " + project.extensions.ao.packageName

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { directoryInput ->
                AOInject.inject(directoryInput.file.absolutePath, project)
                def dest = transformInvocation.outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { jarInput ->
                def jarName = jarInput.name
                println("jar = " + jarInput.file.getAbsolutePath())
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }

        println "AOTransform end..."
    }
}
