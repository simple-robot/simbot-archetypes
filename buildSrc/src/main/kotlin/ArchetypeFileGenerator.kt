/**
 * 构建 `archetype-metadata.xml` 文件内容.
 *
 * @see ArchetypeMetadata
 */
fun archetypeMetadataFileText(archetypeMetadata: ArchetypeMetadata) = buildString {
    val (name, fileSets) = archetypeMetadata
    
    tag("archetype-descriptor", {
        attr("xmlns", "https://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.1.0")
        attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        attr(
            "xsi:schemaLocation",
            "https://maven.apache.org/plugins/maven-archetype-plugin/archetype-descriptor/1.1.0 http://maven.apache.org/xsd/archetype-descriptor-1.1.0.xsd"
        )
        attr("name", name)
    }) {
        if (fileSets.isNotEmpty()) {
            tag("fileSets") {
                for (fileSet in fileSets) {
                    tag("fileSet", {
                        attr("filtered", fileSet.filtered.toString())
                        attr("packaged", fileSet.packaged.toString())
                        attr("encoding", fileSet.encoding)
                    }) {
                        val dir = fileSet.directory
                        if (dir != null) {
                            tag("directory", text = dir)
                        }
                        val excludes = fileSet.excludes
                        if (excludes.isNotEmpty()) {
                            tag("excludes") {
                                for (exclude in excludes) {
                                    tag("exclude", text = exclude)
                                }
                            }
                        }
                        val includes = fileSet.includes
                        if (includes.isNotEmpty()) {
                            tag("includes") {
                                for (include in includes) {
                                    tag("include", text = include)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


data class ArchetypeMetadata(
    val name: String,
    val fileSets: List<FileSet>,
)

data class FileSet(
    val filtered: Boolean = true,
    val packaged: Boolean = true,
    val encoding: String = "UTF-8",
    val directory: String?,
    val excludes: List<String> = emptyList(),
    val includes: List<String> = emptyList(),
)


fun archetypeFileText(archetype: Archetype) = buildString {
    tag("archetype") {
        tag("id", text = archetype.id)
        val sources = archetype.sources
        if (sources.isNotEmpty()) {
            tag("sources") {
                for (source in sources) {
                    tag("source", text = source)
                }
            }
        }
        val testSources = archetype.testSources
        if (testSources.isNotEmpty()) {
            tag("testSources") {
                for (testSource in testSources) {
                    tag("testSource", text = testSource)
                }
            }
        }
    }
}

data class Archetype(val id: String, val sources: List<String>, val testSources: List<String>)


private inline fun StringBuilder.tag(
    tagName: String,
    more: StringBuilder.() -> Unit = {},
    inner: StringBuilder.() -> Unit,
) {
    append('<')
    append(tagName)
    more()
    append('>')
    inner()
    append("</")
    append(tagName)
    append('>')
}

private inline fun StringBuilder.tag(tagName: String, more: StringBuilder.() -> Unit = {}, text: String) =
    tag(tagName, more) { append(text) }

// 'name="value" '

private fun StringBuilder.attr(name: String, value: String) {
    append(' ').append(name).append("=\"").append(value).append('"')
}