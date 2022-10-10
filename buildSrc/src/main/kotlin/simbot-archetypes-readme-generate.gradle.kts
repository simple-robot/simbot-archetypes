import org.gradle.api.provider.Property

interface ReadmeGenerateExtension {
    val version: Property<String>
    val readmeTemplateFile: Property<String?>
    val readmeFile: Property<String?>
    val properties: MapProperty<String, String>
}

// ${{xxx}}$

val readmeGenerate = extensions.create<ReadmeGenerateExtension>("readmeGenerate")

val markRegex = Regex("\\$\\{\\{(?<M>[a-zA-Z0-9_-]+|\\.+)\\}\\}\\$")

val readmeVersionUpdateTask = tasks.create("readmeVersionGenerate") {
    group = "documentation"
    doFirst {
        val readmeTmp = project.file(readmeGenerate.readmeFile.getOrElse("README.mdtmp"))
           .takeIf { it.exists() && it.isFile && it.canWrite() && it.canRead() } ?: return@doFirst

        val readme = project.file(readmeGenerate.readmeFile.getOrElse("README.md"))
           .takeIf { it.exists() && it.isFile && it.canWrite() && it.canRead() } ?: return@doFirst
        
        val deleteMark = deleteMark(false)

        readme.writeText(readmeTmp.useLines { lines ->
            lines.map {
                it.processMarks(deleteMark)
            }.map {
                markRegex.replace(it) { result ->
                    val value = result.value
                    val markName = result.groups.get("M") ?: return@replace value
    
                    readmeGenerate.properties.getting(markName.value).getOrElse(value)
                }
            }.joinToString("\n")
        })
        
    }
}