package com.github.seelikes.groovy.plugin.ao

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AOPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new AOTransform(project))

        project.task('aoTransform') {
            doLast {
                System.out.println('+++++++++++++++++++++aoTransform task')
            }
        }
    }
}
