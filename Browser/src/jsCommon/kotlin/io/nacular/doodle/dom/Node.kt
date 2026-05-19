package io.nacular.doodle.dom

    internal actual inline operator fun NodeList.get(index: Int): Node? = this.item(index)

internal actual abstract external class NodeList actual constructor() {
    actual abstract val length: Int

    actual open fun item(index: Int): Node?
}

public actual abstract external class Node: JsAny {
    internal actual fun cloneNode    (deep : Boolean           ): Node
    internal actual fun appendChild  (node : Node              ): Node
    internal actual fun removeChild  (child: Node              ): Node
    internal actual fun insertBefore (node : Node, child: Node?): Node
    internal actual fun replaceChild (node : Node, child: Node ): Node
    internal actual fun contains     (other: Node?             ): Boolean
    internal actual fun hasChildNodes(                         ): Boolean

    internal actual val nodeName       : String
    internal actual val firstChild     : Node?
    internal actual val parentNode     : Node?
    internal actual val childNodes     : NodeList
    internal actual val nextSibling    : Node?
    internal actual var textContent    : String?
    internal actual val parentElement  : Element?
    internal actual val previousSibling: Node?
}

internal actual fun Node.clear() {
    while (hasChildNodes()) {
        removeChild(firstChild!!)
    }
}