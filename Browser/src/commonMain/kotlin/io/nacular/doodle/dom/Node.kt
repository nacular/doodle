@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect abstract external class NodeList() {
    abstract val length: Int

    open fun item(index: Int): Node?
}

internal expect inline operator fun NodeList.get(index: Int): Node?

internal expect abstract external class Node: JsAny {
    internal fun cloneNode    (deep : Boolean           ): Node
    internal fun appendChild  (node : Node              ): Node
    internal fun removeChild  (child: Node              ): Node
    internal fun insertBefore (node : Node, child: Node?): Node
    internal fun replaceChild (node : Node, child: Node ): Node
    internal fun contains     (other: Node?             ): Boolean
    internal fun hasChildNodes(                         ): Boolean

    internal val nodeName       : String
    internal val firstChild     : Node?
    internal val parentNode     : Node?
    internal val childNodes     : NodeList
    internal val nextSibling    : Node?
    internal var textContent    : String?
    internal val parentElement  : Element?
    internal val previousSibling: Node?
}

internal expect fun Node.clear()