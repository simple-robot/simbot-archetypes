import org.gradle.api.XmlProvider
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

fun Element.firstElement(predicate: (Element.() -> Boolean)) =
    childNodes
        .run { (0 until length).map(::item) }
        .filterIsInstance<Element>()
        .firstOrNull { it.predicate() }


fun Element.lastElement(predicate: (Element.() -> Boolean)) =
    childNodes
        .run { (0 until length).map(::item) }
        .filterIsInstance<Element>()
        .lastOrNull { it.predicate() }


fun Document.element(name: String, configure: Element.() -> Unit = {}): Element = createElement(name).apply(configure)
