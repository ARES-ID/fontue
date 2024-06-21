import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.zip.ZipInputStream
import kotlinx.coroutines.runBlocking

abstract class FontueTask : DefaultTask() {
    @get:Input
    abstract val message: Property<URI>

    @TaskAction
    fun action() = runBlocking {
        val client = HttpClient(CIO) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
        }
        val response = client.get(message.get().toURL())
        ByteArrayInputStream(response.readBytes()).use { byteArrayInputStream ->
            ZipInputStream(byteArrayInputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith(".ttf", ignoreCase = true)) {
                        val path = "${System.getenv("HOME")}/Downloads/${entry.name}"
                        File(path).also { file ->
                            println(file.absolutePath)
                            if (!file.exists()) {
                                file.parentFile.mkdirs()
                                file.createNewFile()
                            }
                        }
                        FileOutputStream(path).use { fileOutputStream ->
                            val buffer = ByteArray(1024)
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
