plugins {
    java
    id("signing")
    id("maven-publish")
}

interface ArchetypeMetadataExtension {
    val name: Property<String>
    val fileSets: ListProperty<FileSet>
}


val archetypeMetadata = extensions.create<ArchetypeMetadataExtension>("archetypeMetadata")


val createArchetypeMetadataFileTask = createArchetypeMetadataFileTask {
    ArchetypeMetadata(archetypeMetadata.name.getOrElse(project.name), archetypeMetadata.fileSets.getOrElse(emptyList()))
}

interface ArchetypeExtension {
    val id: Property<String>
}

val archetype = extensions.create<ArchetypeExtension>("archetype")

val createArchetypeFileTask = createArchetypeFileTask {
    val sources = mutableListOf<String>()
    
    sourceSets.main.get().java.sourceDirectories.asFileTree.matching {
        include("**/*.java")
    }.visit {
        if (!isDirectory) {
            sources.add("src/main/java/$relativePath")
        }
    }
    
    Archetype(archetype.id.getOrElse(project.name), sources = sources, testSources = emptyList())
}



val archetypePomName = "archetypePom"
val archetypePomGenerateTaskName = "generatePomFileFor${"${archetypePomName.first().toUpperCase()}${archetypePomName.substring(1)}"}Publication"

val archetypeSourceJar = createArchetypeSourceJarTask("archetypeSourceJar", "source", archetypePomGenerateTaskName, createArchetypeMetadataFileTask, createArchetypeFileTask)
val archetypeJar = createArchetypeSourceJarTask("archetypeJar", null, archetypePomGenerateTaskName, createArchetypeMetadataFileTask, createArchetypeFileTask)


tasks.jar {
    enabled = false
}

val jarJavadoc by emptyJavadocJar

abstract class ArchetypeMavenPomExtension {
    abstract val projectBuildSourceEncoding: Property<String>
    abstract val mavenCompilerSource: Property<String>
    abstract val mavenCompilerTarget: Property<String>
    
    internal val pomXmlConfigurations = mutableListOf<PomXmlBuilder.() -> Unit>()
    
    fun inPomXml(builder: PomXmlBuilder.() -> Unit) {
        pomXmlConfigurations.add(builder)
    }
    
}

val sonatypeUsername: String? = systemProp("OSSRH_USER")
val sonatypePassword: String? = systemProp("OSSRH_PASSWORD")

publishing {
    publications {
        create<MavenPublication>(archetypePomName) {
            from(components["java"])
            setArtifacts(listOf(archetypeJar, archetypeSourceJar, jarJavadoc))
            groupId = P.GROUP
            artifactId = project.name
            version = P.VERSION
            pom {
                properties.apply {
                    this["project.build.sourceEncoding"] = "UTF-8"
                    this["maven.compiler.source"] = "11"
                    this["maven.compiler.target"] = "11"
                }
                
                inXml("") {
                    appendDependencyElement(
                        "org.junit.jupiter",
                        "junit-jupiter-engine",
                        "5.9.0",
                        "test"
                    )
                    
                    build.append("sourceDirectory") { textContent = "src/main/java" }
                    build.append("testSourceDirectory") { textContent = "src/test/java" }
                    build.append("resources") {
                        append("resource") {
                            append("directory"){ textContent = "src/main/resources" }
                        }
                    }
                    build.append("testResources") {
                        append("testResource") {
                            append("directory"){ textContent = "src/test/resources" }
                        }
                    }
                    appendPluginElement(null, "maven-surefire-plugin", "2.22.2")
                    appendPluginElement(null, "maven-failsafe-plugin", "2.22.2")
                    appendPluginElement("org.codehaus.mojo", "exec-maven-plugin", "1.6.0") {
                        append("configuration") {
                            appendChild(document.createComment("你的main函数所在类"))
                            append("mainClass") { textContent = "\${groupId}.Main" }
                        }
                    }
                }
            }
        }
        
        create<MavenPublication>("archetype") {
            from(components["java"])
            setArtifacts(listOf(archetypeJar, archetypeSourceJar, jarJavadoc))
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            pom {
                name.set(project.name)
                description.set(project.description ?: P.DESCRIPTION)
                url.set("https://github.com/simple-robot/simbot-archetypes")
                licenses {
                    license {
                        name.set("GNU GENERAL PUBLIC LICENSE, Version 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0-standalone.html")
                    }
                    license {
                        name.set("GNU LESSER GENERAL PUBLIC LICENSE, Version 3")
                        url.set("https://www.gnu.org/licenses/lgpl-3.0-standalone.html")
                    }
                }
                scm {
                    url.set("https://github.com/simple-robot/simbot-archetypes")
                    connection.set("scm:git:https://github.com/simple-robot/simbot-archetypes.git")
                    developerConnection.set("scm:git@github.com:simple-robot/simbot-archetypes.git")
                }
                setupDevelopers()
    
            }
        }
        repositories {
            mavenLocal()
            if (project.version.toString().contains("SNAPSHOT", true)) {
                configPublishMaven(Sonatype.Snapshot, sonatypeUsername, sonatypePassword)
            } else {
                configPublishMaven(Sonatype.Central, sonatypeUsername, sonatypePassword)
            }
        }
    }
}

val keyId = systemProp("GPG_KEY_ID")
val secretKey = systemProp("GPG_SECRET_KEY")
val password = systemProp("GPG_PASSWORD")

if (keyId != null) {
    logger.info("Signing property [keyId] is {}", keyId)
    signing {
        // setRequired {
        //     !project.version.toString().endsWith("SNAPSHOT")
        // }
        
        useInMemoryPgpKeys(keyId, secretKey, password)
        
        sign(publishing.publications)
    }
} else {
    logger.warn("Signing property [keyId] (from system env [GPG_KEY_ID]) is null.")
}

fun RepositoryHandler.configPublishMaven(sonatype: Sonatype, username: String?, password: String?) {
    maven {
        name = sonatype.name
        url = uri(sonatype.url)
        credentials {
            this.username = username
            this.password = password
        }
    }
}

fun MavenPom.setupDevelopers() {
    developers {
        developer {
            id.set("forte")
            name.set("ForteScarlet")
            email.set("ForteScarlet@163.com")
            url.set("https://github.com/ForteScarlet")
        }
    }
}

operator fun MapProperty<String, String>.set(key: String, value: String) {
    put(key, value)
}


// /**
//  * Retrieves the [kotlin][org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension] extension.
//  */
// val org.gradle.api.Project.`kotlin`: org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension get() =
//     (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin") as org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

