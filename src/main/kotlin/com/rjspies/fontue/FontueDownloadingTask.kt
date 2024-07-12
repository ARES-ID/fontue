package com.rjspies.fontue

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
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

public abstract class FontueDownloadingTask : DefaultTask() {
    @get:Input
    public abstract val fontUri: Property<URI>

    @get:InputDirectory
    public abstract val targetDirectory: Property<File>

    @TaskAction
    public fun action() {
        runBlocking(Dispatchers.IO) {
            val client = httpClient()
            val response = client.get(fontUri.get().toURL())
            val responseBytes = response.readBytes()
            val byteArrayInputStream = ByteArrayInputStream(responseBytes)
            val zipInputStream = ZipInputStream(byteArrayInputStream)
            val files = unzipFontFiles(zipInputStream)
            byteArrayInputStream.close()
            zipInputStream.close()
            writeFontFiles(files)
            client.close()
        }
    }

    private fun writeFontFiles(files: Map<String, ByteArray>) {
        files.forEach { (formattedName, bytes) ->
            val path = "${targetDirectory.get().absolutePath}${File.separator}$formattedName"
            File(path).also { file ->
                if (!file.exists()) {
                    file.parentFile.mkdirs()
                    file.createNewFile()
                    logger.debug("New file: {}", file.absolutePath)
                }
            }

            FileOutputStream(path).use { fileOutputStream ->
                fileOutputStream.write(bytes, 0, bytes.size)
            }
        }
    }

    private fun unzipFontFiles(zipInputStream: ZipInputStream): Map<String, ByteArray> {
        var entry = zipInputStream.nextEntry
        val files = mutableMapOf<String, ByteArray>()

        while (entry != null) {
            if (entry.name.endsWith(".ttf", ignoreCase = true) && FontName.entries.contains(entry.name)) {
                files += entry.name.format() to zipInputStream.readBytes()
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }

        return files
    }

    private fun httpClient(): HttpClient = HttpClient(CIO) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    this@FontueDownloadingTask.logger.debug(message)
                }
            }
            level = LogLevel.ALL
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

    private enum class FontName(
        val originalNameWithoutExtension: String,
        val targetNameWithoutExtension: String,
    ) {
        Thin("Phosphor-Thin", "phosphor_thin"),
        Light("Phosphor-Light", "phosphor_light"),
        Regular("Phosphor", "phosphor_regular"),
        Bold("Phosphor-Bold", "phosphor_bold"),
        Fill("Phosphor-Fill", "phosphor_fill"),
    }
}
