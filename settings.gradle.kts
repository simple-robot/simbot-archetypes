rootProject.name = "simple-robot-archetypes"

include0(":maven-kotlin") { "simbot-maven-kotlin-archetype" }
include0(":maven-java") { "simbot-maven-java-archetype" }
// include0(":maven-java-spring-boot") { "simbot-maven-java-spring-boot-archetype" }


inline fun include0(path: String, project: () -> String? = { null }) {
    include(path)
    val projectName = project()
    if (projectName != null) {
        project(path).name = projectName
    }
}