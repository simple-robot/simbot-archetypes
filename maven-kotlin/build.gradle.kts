import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `simbot-archetypes-archetype-maven-kotlin-publish`
    `simbot-archetypes-readme-generate`
}

repositories {
    mavenCentral()
}

dependencies {
    testApi(kotlin("test"))
    api("love.forte.simbot:simbot-core:${P.SIMBOT_VERSION}")
    //api("love.forte.simbot.component:simbot-component-mirai-core:3.0.0.0-beta-M3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val archetypeId = project.name

archetypeMetadata {
    name.set(archetypeId)
    fileSets.add(FileSet(directory = "src/main/kotlin"))
    fileSets.add(FileSet(directory = "src/test/kotlin"))
}

archetype {
    id.set(archetypeId)
}

readmeGenerate {
    properties.put("VERSION", P.VERSION)
    properties.put("GROUP", P.GROUP)
    properties.put("ARTIFACT", project.name)
}
