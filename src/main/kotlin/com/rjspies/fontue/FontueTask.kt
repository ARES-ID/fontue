package com.rjspies.fontue

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public abstract class FontueTask : DefaultTask() {
    @TaskAction
    public fun action(): Unit = Unit
}
