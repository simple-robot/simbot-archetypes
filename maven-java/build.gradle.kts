plugins {
    `java-library`
    `simbot-archetypes-archetype-maven-java-publish`
    `simbot-archetypes-readme-generate`
}


dependencies {
    api("love.forte.simbot:simbot-core:3.0.0-beta.3")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    //api("love.forte.simbot.component:simbot-component-mirai-core:3.0.0.0-beta-M3")
}

tasks.test {
    useJUnitPlatform()
}

val archetypeId = project.name
repositories {
    mavenCentral()
}

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