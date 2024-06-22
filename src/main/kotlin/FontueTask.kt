import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

internal abstract class FontueTask : DefaultTask() {
    @TaskAction
    fun action() = Unit
}
