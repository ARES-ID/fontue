import org.gradle.api.provider.Property
import java.io.File
import java.net.URI
import java.nio.file.Path

public interface FontueConfiguration {
    public val fontUri: Property<URI>
    public val targetDirectory: Property<File>
}
