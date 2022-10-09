plugins {
    kotlin("jvm")
    id("signing")
    id("maven-publish")
}

interface ArchetypeMetadataExtension {
    val name: Property<String>
    val fileSets: ListProperty<FileSet>
}


val archetypeMetadata = extensions.create<ArchetypeMetadataExtension>("archetypeMetadata")

val createArchetypeMetadataFileTask = tasks.create("createArchetypeMetadataFile") {
    val outputFile = project.buildDir.resolve("archetypes/archetype-metadata.xml")
    
    doFirst {
        val archetypeFileText = archetypeMetadataFileText(
            ArchetypeMetadata(archetypeMetadata.name.get(), archetypeMetadata.fileSets.getOrElse(emptyList()))
        )
        if (outputFile.exists()) {
            outputFile.delete()
        }
        outputFile.appendText(archetypeFileText)
    }
    outputs.files(outputFile)
}

interface ArchetypeExtension {
    val id: Property<String>
}

val archetype = extensions.create<ArchetypeExtension>("archetype")


val createArchetypeFileTask = tasks.create("createArchetypeFile") {
    val outputFile = project.buildDir.resolve("archetypes/archetype.xml")
    
    doFirst {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        val sources = mutableListOf<String>()
    
        sourceSets.main.get().kotlin.sourceDirectories.asFileTree.matching {
            include("**/*.kt")
        }.visit {
            if (!isDirectory) {
                sources.add("src/main/kotlin/$relativePath")
            }
        }
    
        val archetypeFileText = archetypeFileText(Archetype(archetype.id.get(), sources = sources, testSources = emptyList()))
        
        outputFile.appendText(archetypeFileText)
    }
    outputs.files(outputFile)
}

val archetypePomName = "archetypePom"
val archetypePomGenerateTaskName = "generatePomFileFor${"${archetypePomName.first().toUpperCase()}${archetypePomName.substring(1)}"}Publication"

val archetypeSourceJar = tasks.register<Jar>("archetypeSourceJar") {
    archiveClassifier.set("sources")
    configMavenArchetypeSourceJar(
        tasks.named(archetypePomGenerateTaskName, GenerateMavenPom::class).get(),
        createArchetypeMetadataFileTask,
        createArchetypeFileTask
    )
}
val archetypeJar = tasks.register<Jar>("archetypeJar") {
    configMavenArchetypeSourceJar(
        tasks.named(archetypePomGenerateTaskName, GenerateMavenPom::class).get(),
        createArchetypeMetadataFileTask,
        createArchetypeFileTask
    )
}

tasks.jar {
    enabled = false
}


val jarJavadoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

abstract class ArchetypeMavenPomExtension {
    abstract val projectBuildSourceEncoding: Property<String>
    abstract val kotlinCodeStyle: Property<String>
    abstract val kotlinCompilerJvmTarget: Property<String>
    abstract val mavenCompilerSource: Property<String>
    abstract val mavenCompilerTarget: Property<String>
    
    abstract val kotlinVersion: Property<String>
    
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
                    this["kotlin.code.style"] = "official"
                    this["kotlin.compiler.jvmTarget"] = "11"
                    this["maven.compiler.source"] = "11"
                    this["maven.compiler.target"] = "11"
                }
                
                inXml(project.kotlin.coreLibrariesVersion) {
                    appendDependencyElement(
                        "org.jetbrains.kotlin",
                        "kotlin-test-junit5",
                        kotlinVersion,
                        "test"
                    )
    
                    appendDependencyElement(
                        "org.junit.jupiter",
                        "junit-jupiter-engine",
                        "5.9.0",
                        "test"
                    )
                    
                    build.append("sourceDirectory") { textContent = "src/main/kotlin" }
                    build.append("testSourceDirectory") { textContent = "src/test/kotlin" }
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
                    appendPluginElement("org.jetbrains.kotlin", "kotlin-maven-plugin", kotlinVersion) {
                        append("executions") {
                            fun appendExecution(id: String, phase: String, goals: List<String>) {
                                append("execution") {
                                    append("id") { textContent = id }
                                    append("phase") { textContent = phase }
                                    if (goals.isNotEmpty()) {
                                        append("goals") {
                                            goals.forEach { goal ->
                                                append("goal") { textContent = goal }
                                            }
                                        }
                                    }
                                }
                            }
            
                            appendExecution("compile", "compile", listOf("compile"))
                            appendExecution("test-compile", "test-compile", listOf("test-compile"))
                        }
                    }
                    appendPluginElement(null, "maven-surefire-plugin", "2.22.2")
                    appendPluginElement(null, "maven-failsafe-plugin", "2.22.2")
                    appendPluginElement("org.codehaus.mojo", "exec-maven-plugin", "1.6.0") {
                        append("configuration") {
                            appendChild(document.createComment("你的main函数所在类"))
                            append("mainClass") { textContent = "\${groupId}.MainKt" }
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


/**
 * Retrieves the [kotlin][org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension] extension.
 */
val org.gradle.api.Project.`kotlin`: org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("kotlin") as org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

