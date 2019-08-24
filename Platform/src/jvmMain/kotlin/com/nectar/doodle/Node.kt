package com.nectar.doodle



/**
 * Created by Nicholas Eddy on 8/9/19.
 */

actual inline operator fun NodeList.get(index: Int): Node? = item(index)

actual abstract class NodeList actual constructor() {
    private val values: List<Node> = mutableListOf()

    actual open val length = values.size

    actual open fun item(index: Int) = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

actual abstract class Node {
    actual fun appendChild (node:  Node) = node
    actual fun insertBefore(node:  Node, child: Node?) = node
    actual fun removeChild (child: Node) = child
    actual fun cloneNode   (deep:  Boolean) = this // FIXME

    actual val nodeName         = ""
    actual val firstChild get() = childNodes.item(0)
    actual val parentNode: Node? = null
    actual val childNodes = object: NodeList() {}
    actual fun replaceChild(node: Node, child: Node) = child

    actual val nextSibling: Node? = null
}

actual fun Node.clear() {}