package io.nacular.doodle.dom


/**
 * Created by Nicholas Eddy on 8/9/19.
 */

internal actual inline operator fun NodeList.get(index: Int): Node? = item(index)

internal actual abstract class NodeList actual constructor() {
    protected val values: List<Node> = mutableListOf()

    actual abstract val length: Int

    actual open fun item(index: Int): Node? = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

internal actual abstract class Node: JsAny {
    actual fun appendChild (node: Node): Node = node
    actual fun insertBefore(node: Node, child: Node?): Node = node
    actual fun removeChild (child: Node): Node = child
    actual fun cloneNode   (deep:  Boolean): Node = this // FIXME
    actual fun replaceChild(node: Node, child: Node): Node = child
    actual fun contains    (other: Node?             ): Boolean = false

    actual val nodeName: String = ""
    actual val firstChild: Node? get() = childNodes.item(0)
    actual val parentNode: Node? = null
    actual val childNodes: NodeList = object: NodeList() {
        override val length get() = values.size
    }
    actual val nextSibling : Node? = null
    actual var textContent : String? = ""
    actual val parentElement: Element? = null
    actual fun hasChildNodes(): Boolean = false
}

internal actual fun Node.clear() {}