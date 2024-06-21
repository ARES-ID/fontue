import org.gradle.api.Plugin
import org.gradle.api.Project

class FontuePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val configuration = target.extensions.create("fontueConfiguration", FontueConfiguration::class.java)
        target.tasks.create("fontue", FontueTask::class.java) { task ->
            task.message.set(configuration.fontFile)
        }
    }
}
