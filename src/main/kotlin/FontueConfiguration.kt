import org.gradle.api.provider.Property
import java.net.URI

interface FontueConfiguration {
    val fontFile: Property<URI>
}
