import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.io.File

fun Project.createArchetypeFileTask(
    taskName: String = "createArchetypeFile",
    outputFile: File = buildDir.resolve("archetypes/archetype.xml"),
    archetypeBlock: Task.() -> Archetype,
) = tasks.create(taskName) {
    
    doFirst {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        
        val archetypeFileText = archetypeFileText(archetypeBlock())
        
        outputFile.appendText(archetypeFileText)
    }
    outputs.files(outputFile)
}
fun Project.createArchetypeMetadataFileTask(
    taskName: String = "createArchetypeMetadataFile",
    outputFile: File = buildDir.resolve("archetypes/archetype-metadata.xml"),
    archetypeMetadataBlock: Task.() -> ArchetypeMetadata,
) = tasks.create(taskName) {
    doFirst {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        val archetypeFileText = archetypeMetadataFileText(archetypeMetadataBlock())
        outputFile.appendText(archetypeFileText)
    }
    outputs.files(outputFile)
}

fun Project.createArchetypeSourceJarTask(
    taskName: String,
    archiveClassifierValue: String?,
    archetypePomGenerateTaskName: String,
    createArchetypeMetadataFileTask: Task,
    createArchetypeFileTask: Task,
) = tasks.register<Jar>(taskName) {
    archiveClassifierValue?.let(archiveClassifier::set)
    
    configMavenArchetypeSourceJar(
        tasks.named(archetypePomGenerateTaskName, GenerateMavenPom::class).get(),
        createArchetypeMetadataFileTask,
        createArchetypeFileTask
    )
}

fun Project.createArchetypeJarTask(
    taskName: String = "archetypeJar",
    archetypePomGenerateTaskName: String,
    createArchetypeMetadataFileTask: Task,
    createArchetypeFileTask: Task,
) = tasks.register<Jar>(taskName) {
    configMavenArchetypeSourceJar(
        tasks.named(archetypePomGenerateTaskName, GenerateMavenPom::class).get(),
        createArchetypeMetadataFileTask,
        createArchetypeFileTask
    )
}