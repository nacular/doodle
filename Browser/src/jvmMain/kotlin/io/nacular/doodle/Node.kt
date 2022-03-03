package io.nacular.doodle



/**
 * Created by Nicholas Eddy on 8/9/19.
 */

public actual inline operator fun NodeList.get(index: Int): Node? = item(index)

public actual abstract class NodeList public actual constructor() {
    protected val values: List<Node> = mutableListOf()

    public actual abstract val length: Int

    public actual open fun item(index: Int): Node? = try {
        values[index]
    } catch (e: Exception) {
        null
    }
}

public actual abstract class Node {
    public actual fun appendChild (node:  Node): Node = node
    public actual fun insertBefore(node:  Node, child: Node?): Node = node
    public actual fun removeChild (child: Node): Node = child
    public actual fun cloneNode   (deep:  Boolean): Node = this // FIXME

    public actual val nodeName: String = ""
    public actual val firstChild: Node? get() = childNodes.item(0)
    public actual val parentNode: Node? = null
    public actual val childNodes: NodeList = object: NodeList() {
        override val length get() = values.size
    }
    public actual fun replaceChild(node: Node, child: Node): Node = child

    public actual val nextSibling: Node? = null
    public actual var textContent: String? = ""
}

public actual fun Node.clear() {}