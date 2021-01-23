package io.nacular.doodle

/**
 * Created by Nicholas Eddy on 8/9/19.
 */

internal expect inline operator fun NodeList.get(index: Int): Node?

public expect abstract class NodeList() {
    public abstract val length: Int

    public open fun item(index: Int): Node?
}

internal expect fun Node.clear()

public expect abstract class Node {
    public fun cloneNode   (deep : Boolean           ): Node
    public fun appendChild (node : Node              ): Node
    public fun removeChild (child: Node              ): Node
    public fun insertBefore(node : Node, child: Node?): Node
    public fun replaceChild(node : Node, child: Node ): Node

    public val nodeName   : String
    public val firstChild : Node?
    public val parentNode : Node?
    public val childNodes : NodeList
    public val nextSibling: Node?
}