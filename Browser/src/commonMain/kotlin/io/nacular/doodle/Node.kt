package io.nacular.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */

expect inline operator fun NodeList.get(index: Int): Node?

expect abstract class NodeList() {
    abstract val length: Int

    open fun item(index: Int): Node?
}

expect fun Node.clear()

expect abstract class Node {
    fun cloneNode   (deep : Boolean           ): Node
    fun appendChild (node : Node              ): Node
    fun removeChild (child: Node              ): Node
    fun insertBefore(node : Node, child: Node?): Node
    fun replaceChild(node : Node, child: Node ): Node

    val nodeName   : String
    val firstChild : Node?
    val parentNode : Node?
    val childNodes : NodeList
    val nextSibling: Node?
}