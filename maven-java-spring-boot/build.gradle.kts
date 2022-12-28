plugins {
    `java-library`
    `simbot-archetypes-archetype-maven-java-spring-boot-publish`
    `simbot-archetypes-readme-generate`
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api("love.forte.simbot.boot:simboot-core-spring-boot-starter:${P.SIMBOT_VERSION}")
    api("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    //api("love.forte.simbot.component:simbot-component-mirai-core:3.0.0.0-beta.1")
}


tasks.test {
    useJUnitPlatform()
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
