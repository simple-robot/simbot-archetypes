import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.named

/**
 * Retrieves the [sourceSets][org.gradle.api.tasks.SourceSetContainer] extension.
 */
internal val org.gradle.api.Project.`sourceSets`: org.gradle.api.tasks.SourceSetContainer get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer


/**
 * Provides the existing [main][org.gradle.api.tasks.SourceSet] element.
 */
internal val org.gradle.api.tasks.SourceSetContainer.`main`: NamedDomainObjectProvider<SourceSet>
    get() = named<org.gradle.api.tasks.SourceSet>("main")

/**
 * Provides the existing [main][org.gradle.api.tasks.SourceSet] element.
 */
internal val org.gradle.api.tasks.SourceSetContainer.`test`: NamedDomainObjectProvider<SourceSet>
    get() = named<org.gradle.api.tasks.SourceSet>("test")


/**
 * Retrieves the [kotlin][org.gradle.api.file.SourceDirectorySet] extension.
 */
val org.gradle.api.tasks.SourceSet.`kotlin`: org.gradle.api.file.SourceDirectorySet get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin") as org.gradle.api.file.SourceDirectorySet
