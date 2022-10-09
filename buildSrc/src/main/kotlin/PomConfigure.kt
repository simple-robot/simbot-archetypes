import org.gradle.api.XmlProvider
import org.gradle.api.publish.maven.MavenPom
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

fun MavenPom.inXml(kotlinVersion: String, builder: PomXmlBuilder.() -> Unit) {
    withXml {
        PomXmlBuilder(kotlinVersion, this).builder()
    }
}

class PomXmlBuilder internal constructor(
    val kotlinVersion: String,
    val xmlProvider: XmlProvider
) {
    val rootElement = xmlProvider.asElement()
    val document = rootElement.ownerDocument
    
    fun element(name: String, configure: Element.() -> Unit = {}): Element = document.element(name, configure)
    fun Node.append(name: String, configure: Element.() -> Unit = {}): Node = appendChild(element(name, configure))
    fun <N : Node> appendElementToRoot(build: Document.() -> N): N {
        return document.build().also(rootElement::appendChild)
    }
    val dependencies = rootElement.firstElement { tagName == "dependencies" } ?: appendElementToRoot { element("dependencies") }
    fun appendDependencyElement(
        groupId: String, artifactId: String, version: String, scope: String, configure: Element.() -> Unit = {}
    ) = element("dependency") {
        append("groupId") { textContent = groupId }
        append("artifactId") { textContent = artifactId }
        append("version") { textContent = version }
        append("scope") { textContent = scope }
    }.also(configure).also(dependencies::appendChild)
    
    val build = appendElementToRoot { element("build") }
    val plugins by lazy { build.append("plugins") }
    
    fun appendPluginElement(
        groupId: String?, artifactId: String?, version: String?, configure: Element.() -> Unit = {},
    ): Element {
        return element("plugin") {
            if (groupId != null) {
                append("groupId") { textContent = groupId }
            }
            if (artifactId != null) {
                append("artifactId") { textContent = artifactId }
            }
            if (version != null) {
                append("version") { textContent = version }
            }
        }.also(configure).also(plugins::appendChild)
    }
    
}
