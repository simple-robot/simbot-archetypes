import org.gradle.api.Action
import org.gradle.api.publish.PublishingExtension

/**
 * Retrieves the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
internal val org.gradle.api.Project.`publishing`: org.gradle.api.publish.PublishingExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("publishing") as org.gradle.api.publish.PublishingExtension


/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
internal fun org.gradle.api.Project.`publishing`(configure: Action<PublishingExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)



