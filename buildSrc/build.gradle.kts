
plugins {
    // kotlin("jvm") version "1.7.20"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin", "1.7.20"))
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
}