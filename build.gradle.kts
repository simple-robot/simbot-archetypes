import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

plugins {
    `java-library`
    kotlin("jvm") version "1.7.20"
    id("maven-publish") // version "1.1.0"
}

group = "love.forte.simbot.archetypes"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testApi(kotlin("test"))
    // testApi()
    api("love.forte.simbot:simbot-core:3.0.0-beta.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val createArchetypeMetadataFileTask = tasks.create("createArchetypeMetadataFile") {
    val outputFile = project.buildDir.resolve("archetypes/archetype-metadata.xml")
    val archetypeFileText = """
        <archetype-descriptor xmlns="https://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.1.0"
                      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xsi:schemaLocation="https://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.1.0 http://maven.apache.org/xsd/archetype-descriptor-1.1.0.xsd"
                      name="simple-robot-archetype-kotlin">
            <fileSets>
                <fileSet filtered="true" packaged="true">
                    <directory>src/main/kotlin</directory>
                </fileSet>
                <fileSet>
                    <directory>src/test/kotlin</directory>
                </fileSet>
            </fileSets>
        </archetype-descriptor>
    """.trimIndent()
    
    doFirst {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        outputFile.appendText(archetypeFileText)
    }
    outputs.files(outputFile)
}

val createArchetypeFileTask = tasks.create("createArchetypeFile") {
    val outputFile = project.buildDir.resolve("archetypes/archetype.xml")
    val archetypeFileText = """
        <archetype>
          <id>simple-robot-archetype-kotlin</id>
          <sources>
            <source>src/main/kotlin/Hello.kt</source>
          </sources>
          <testSources>
          </testSources>
        </archetype>
    """.trimIndent()
    
    doFirst {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        outputFile.appendText(archetypeFileText)
    }
    outputs.files(outputFile)
}

fun Jar.configArchetypeSourceJar() {
    val generateMavenPomTask = tasks.withType(GenerateMavenPom::class).firstOrNull() ?: return
    dependsOn(generateMavenPomTask)
    dependsOn(createArchetypeMetadataFileTask)
    dependsOn(createArchetypeFileTask)
    
    val main = sourceSets.main.get()
    val test = sourceSets.test.get()
    val sourceDir = "archetype-resources"
    val kotlinSourceDir = "$sourceDir/src/main/kotlin"
    val javaSourceDir = "$sourceDir/src/main/java"
    val kotlinTestSourceDir = "$sourceDir/src/test/kotlin"
    val javaTestSourceDir = "$sourceDir/src/test/java"
    
    val metaInfMaven = "META-INF/maven"
    
    from(main.kotlin) {
        into(kotlinSourceDir)
    }
    from(main.java) {
        into(javaSourceDir)
    }
    from(test.kotlin) {
        into(kotlinTestSourceDir)
    }
    from(test.java) {
        into(javaTestSourceDir)
    }
    from(main.resources)
    
    from(generateMavenPomTask.destination) {
        into(sourceDir)
        rename { "pom.xml" }
    }
    
    from(createArchetypeMetadataFileTask.outputs) {
        into(metaInfMaven)
    }
    from(createArchetypeFileTask.outputs) {
        into(metaInfMaven)
    }
}


val archetypeSourceJar = tasks.register<Jar>("archetypeSourceJar") {
    archiveClassifier.set("sources")
    configArchetypeSourceJar()
}
val archetypeJar = tasks.register<Jar>("archetypeJar") {
    // archiveClassifier.set("archetype")
    configArchetypeSourceJar()
}

tasks.jar {
    enabled = false
}


val jarJavadoc by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenPublication") {
            from(components["java"])
            this.setArtifacts(listOf(archetypeJar, archetypeSourceJar, jarJavadoc))
            
            pom {
                properties.apply {
                    this["project.build.sourceEncoding"] = "UTF-8"
                    this["kotlin.code.style"] = "official"
                    this["kotlin.compiler.jvmTarget"] = "1.8"
                    this["maven.compiler.source"] = "8"
                    this["maven.compiler.target"] = "8"
                }
                
                withXml {
                    val kotlinVersion = project.kotlin.coreLibrariesVersion
                    fun Element.firstElement(predicate: (Element.() -> Boolean)) =
                        childNodes
                            .run { (0 until length).map(::item) }
                            .filterIsInstance<Element>()
                            .firstOrNull { it.predicate() }
                    
                    fun Element.lastElement(predicate: (Element.() -> Boolean)) =
                        childNodes
                            .run { (0 until length).map(::item) }
                            .filterIsInstance<Element>()
                            .lastOrNull { it.predicate() }
                    
                    val rootElement = asElement()
                    val document = rootElement.ownerDocument
    
                    fun element(name: String, configure: Element.() -> Unit = {}) = document.createElement(name).apply(configure)
                    fun Node.append(name: String, configure: Element.() -> Unit = {}) = appendChild(element(name, configure))
                    
                    fun <N : Node> appendElementToRoot(build: Document.() -> N): N {
                        return document.build().also(rootElement::appendChild)
                    }
                    
                    // region dependencies
                    val dependencies = rootElement.firstElement { tagName == "dependencies" } ?: appendElementToRoot { element("dependencies") }
                    
                    fun appendDependencyElement(
                        groupId: String, artifactId: String, version: String, scope: String, configure: Element.() -> Unit = {}
                    ) = element("dependency") {
                        append("groupId") { textContent = groupId }
                        append("artifactId") { textContent = artifactId }
                        append("version") { textContent = version }
                        append("scope") { textContent = scope }
                    }.also(configure).also(dependencies::appendChild)
                    
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
                    // endregion
                    
                    val build = appendElementToRoot { element("build") }
                    // <sourceDirectory>src/main/kotlin</sourceDirectory>
                    // <testSourceDirectory>src/test/kotlin</testSourceDirectory>
                    build.append("sourceDirectory") { textContent = "src/main/kotlin" }
                    build.append("testSourceDirectory") { textContent = "src/test/kotlin" }
                    
                    val plugins = build.append("plugins")
    
                    fun appendPluginElement(
                        groupId: String?, artifactId: String?, version: String?, configure: Element.() -> Unit = {},
                    ): Element {
                        return element("plugin") {
                            if (groupId != null) {
                                append("groupId") { textContent = groupId }
                            }
                            if (artifactId != null) {
                                append("artifactId") { textContent = artifactId }
                            }
                            if (version != null) {
                                append("version") { textContent = version }
                            }
                        }.also(configure).also(plugins::appendChild)
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
                            append("mainClass") { textContent = "MainKt" }
                        }
                    }
                    
                }
                
            }
        }
    }
}

operator fun MapProperty<String, String>.set(key: String, value: String) {
    put(key, value)
}



