package com.rjspies.fontue

import org.gradle.api.Plugin
import org.gradle.api.Project

public class FontuePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val configuration = target.extensions.create("fontueConfiguration", FontueConfiguration::class.java)
        target.tasks.create("fontueDownload", FontueDownloadingTask::class.java) { task ->
            task.fontUri.set(configuration.fontUri)
            task.targetDirectory.set(configuration.targetDirectory)
        }

        target.tasks.create("fontue", FontueTask::class.java) { task ->
            task.dependsOn("fontueDownload")
        }
    }
}
