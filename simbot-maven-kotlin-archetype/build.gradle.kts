import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `simbot-archetypes-archetype-maven-publish`
}


dependencies {
    testApi(kotlin("test"))
    api("love.forte.simbot:simbot-core:3.0.0-beta.2")
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
