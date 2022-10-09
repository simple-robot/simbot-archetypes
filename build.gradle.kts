plugins {
    `simbot-archetypes-nexus-publish`
}

group = P.GROUP
version = P.VERSION

repositories {
    mavenLocal()
    mavenCentral()
}

allprojects {
    group = P.GROUP
    version = P.VERSION
    repositories {
        mavenLocal()
        mavenCentral()
    }
}
