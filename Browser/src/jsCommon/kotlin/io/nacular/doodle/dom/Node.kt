package io.nacular.doodle.dom

    internal actual inline operator fun NodeList.get(index: Int): Node? = this.item(index)

internal actual abstract external class NodeList actual constructor() {
    actual abstract val length: Int

    actual open fun item(index: Int): Node?
}

internal actual abstract external class Node: JsAny {
    actual fun cloneNode    (deep : Boolean           ): Node
    actual fun appendChild  (node : Node              ): Node
    actual fun removeChild  (child: Node              ): Node
    actual fun insertBefore (node : Node, child: Node?): Node
    actual fun replaceChild (node : Node, child: Node ): Node
    actual fun contains     (other: Node?             ): Boolean
    actual fun hasChildNodes(                         ): Boolean

    actual val nodeName       : String
    actual val firstChild     : Node?
    actual val parentNode     : Node?
    actual val childNodes     : NodeList
    actual val nextSibling    : Node?
    actual var textContent    : String?
    actual val parentElement  : Element?
    actual val previousSibling: Node?
}

internal actual fun Node.clear() {
    while (hasChildNodes()) {
        removeChild(firstChild!!)
    }
}