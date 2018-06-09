package com.github.seelikes.groovy.plugin.ao

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AOPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new AOTransform(project))

        android.applicationVariants.all { variant ->
            def variantData = variant.variantData
            def scope = variantData.scope

            def aoTransform = project.task(scope.getTaskName("aoTransform", "AOPlugin"))
            aoTransform.doLast {
                System.out.println('+++++++++++++++++++++aoTransform ao ao ao')
            }
            def generateBuildConfig = project.tasks.getByName(scope.getGenerateBuildConfigTask().name)
            if (generateBuildConfig) {
                aoTransform.dependsOn generateBuildConfig
                generateBuildConfig.finalizedBy aoTransform
            }
        }
    }
}
