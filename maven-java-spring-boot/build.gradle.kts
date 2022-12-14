import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `simbot-archetypes-archetype-maven-kotlin-publish`
    `simbot-archetypes-readme-generate`
    id("org.springframework.boot") version "2.7.5"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
}


dependencies {
    api("love.forte.simbot.boot:simboot-core-spring-boot-starter:3.0.0-beta.3")
    //api("love.forte.simbot.component:simbot-component-mirai-core:3.0.0.0-beta.1")
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
    fileSets.add(FileSet(directory = "src/main/java"))
    fileSets.add(FileSet(directory = "src/test/java"))
}

archetype {
    id.set(archetypeId)
}

readmeGenerate {
    properties.put("VERSION", P.VERSION)
    properties.put("GROUP", P.GROUP)
    properties.put("ARTIFACT", project.name)
}