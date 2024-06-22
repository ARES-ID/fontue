import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.VisibleForTesting
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.zip.ZipInputStream
import kotlinx.coroutines.runBlocking

private const val INITIAL_SIZE = 1024

internal abstract class FontueDownloadingTask : DefaultTask() {
    @get:Input
    abstract val fontUri: Property<URI>

    @get:InputDirectory
    abstract val targetDirectory: Property<File>

    @TaskAction
    fun action() {
        runBlocking {
            val client = httpClient()
            val response = client.get(fontUri.get().toURL())
            ByteArrayInputStream(response.readBytes()).use { byteArrayInputStream ->
                ZipInputStream(byteArrayInputStream).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {
                        if (entry.name.endsWith(".ttf", ignoreCase = true) && FontName.entries.contains(entry.name)) {
                            val path = "${targetDirectory.get().absolutePath}${File.separator}${entry.name.format()}"
                            File(path).also { file ->
                                logger.debug("new file: {}", file.absolutePath)
                                if (!file.exists()) {
                                    file.parentFile.mkdirs()
                                    file.createNewFile()
                                }
                            }
                            FileOutputStream(path).use { fileOutputStream ->
                                val buffer = ByteArray(INITIAL_SIZE)
                                var length: Int
                                while (zipInputStream.read(buffer).also { length = it } > 0) {
                                    fileOutputStream.write(buffer, 0, length)
                                }
                            }
                        }
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                }
            }
            client.close()
        }
    }

    private fun httpClient(): HttpClient {
        return HttpClient(CIO) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        getLogger().debug(message)
                    }
                }
                level = LogLevel.ALL
            }
        }
    }

    private fun String.format(): String {
        val temporaryFile = File(this)
        val discriminatedName = temporaryFile.nameWithoutExtension
        val extension = temporaryFile.extension
        val fontName = FontName.entries.first { it.originalNameWithoutExtension == discriminatedName }
        return "${fontName.targetNameWithoutExtension}.$extension"
    }

    private fun List<FontName>.contains(originalName: String): Boolean {
        val temporaryFile = File(originalName)
        return any { it.originalNameWithoutExtension == temporaryFile.nameWithoutExtension }
    }
}

@VisibleForTesting
internal enum class FontName(
    val originalNameWithoutExtension: String,
    val targetNameWithoutExtension: String
) {
    Thin("Phosphor-Thin", "phosphor_thin"),
    Light("Phosphor-Light", "phosphor_light"),
    Regular("Phosphor", "phosphor_regular"),
    Bold("Phosphor-Bold", "phosphor_bold"),
    Fill("Phosphor-Fill", "phosphor_fill"),
}
