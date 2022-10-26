plugins {
    `java-library`
    `simbot-archetypes-archetype-maven-publish`
    `simbot-archetypes-readme-generate`
}


dependencies {
    testApi(kotlin("test"))
    api("love.forte.simbot:simbot-core:3.0.0-beta.2")
    //api("love.forte.simbot.component:simbot-component-mirai-core:3.0.0.0-beta-M3")
}

tasks.test {
    useJUnitPlatform()
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