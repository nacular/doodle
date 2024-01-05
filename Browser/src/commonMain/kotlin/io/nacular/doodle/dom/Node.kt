@file:Suppress("EXPECTED_EXTERNAL_DECLARATION", "WRONG_MODIFIER_TARGET")

package io.nacular.doodle.dom

internal expect abstract external class NodeList() {
    abstract val length: Int

    open fun item(index: Int): Node?
}

internal expect inline operator fun NodeList.get(index: Int): Node?

internal expect abstract external class Node: JsAny {
    fun cloneNode    (deep : Boolean           ): Node
    fun appendChild  (node : Node              ): Node
    fun removeChild  (child: Node              ): Node
    fun insertBefore (node : Node, child: Node?): Node
    fun replaceChild (node : Node, child: Node ): Node
    fun contains     (other: Node?             ): Boolean
    fun hasChildNodes(                         ): Boolean

    val nodeName     : String
    val firstChild   : Node?
    val parentNode   : Node?
    val childNodes   : NodeList
    val nextSibling  : Node?
    var textContent  : String?
    val parentElement: Element?
}

internal expect fun Node.clear()