import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.registering

val Project.emptyJavadocJar
    get() = tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }

fun Jar.configMavenArchetypeSourceJar(
    generateMavenPomTask: GenerateMavenPom,
    createArchetypeMetadataFileTask: Task,
    createArchetypeFileTask: Task,
) {
    // val generateMavenPomTask = tasks.withType(GenerateMavenPom::class).firstOrNull() ?: return
    dependsOn(generateMavenPomTask)
    dependsOn(createArchetypeMetadataFileTask)
    dependsOn(createArchetypeFileTask)
    
    val main = project.sourceSets.main.get()
    val test = project.sourceSets.test.get()
    val sourceDir = "archetype-resources"
    val kotlinSourceDir = "$sourceDir/src/main/kotlin"
    val javaSourceDir = "$sourceDir/src/main/java"
    val resourcesDir = "$sourceDir/src/main/resources"
    
    val kotlinTestSourceDir = "$sourceDir/src/test/kotlin"
    val javaTestSourceDir = "$sourceDir/src/test/java"
    val testResourcesDir = "$sourceDir/src/test/resources"
    
    val metaInfMaven = "META-INF/maven"
    
    main.kotlinOrNull?.let { kt ->
        from(kt) {
            into(kotlinSourceDir)
        }
    }
    
    from(main.java) {
        into(javaSourceDir)
    }
    from(main.resources) {
        into(resourcesDir)
    }
    
    test.kotlinOrNull?.let { kt ->
        from(kt) {
            into(kotlinTestSourceDir)
        }
    }
    
    from(test.java) {
        into(javaTestSourceDir)
    }
    from(test.resources) {
        into(testResourcesDir)
    }
    
    val deleteMark = deleteMark(false)
    
    filter { it.processMarks(deleteMark) }
    
    from(generateMavenPomTask.destination) {
        into(sourceDir)
        rename { "pom.xml" }
        filter {
            val lineTrim = it.trim()
            when {
                lineTrim == "<groupId>${P.GROUP}</groupId>" -> "  <groupId>\${groupId}</groupId>"
                lineTrim == "<artifactId>${project.name}</artifactId>" -> "  <artifactId>\${artifactId}</artifactId>"
                lineTrim == "<version>${P.VERSION}</version>" -> "  <version>\${version}</version>"
                else -> it
            }
        }
    }
    
    from(createArchetypeMetadataFileTask.outputs) {
        into(metaInfMaven)
    }
    from(createArchetypeFileTask.outputs) {
        into(metaInfMaven)
    }
}


/**
 * Provides the existing [jar][org.gradle.api.tasks.bundling.Jar] task.
 */
internal val TaskContainer.`jar`: TaskProvider<Jar>
    get() = named<Jar>("jar")


