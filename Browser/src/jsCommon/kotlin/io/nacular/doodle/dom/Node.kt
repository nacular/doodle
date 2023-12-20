package io.nacular.doodle.dom

import kotlinx.dom.clear
import org.w3c.dom.get

/**
 * Created by Nicholas Eddy on 8/9/19.
 */

internal actual inline operator fun NodeList.get(index: Int): Node? = get(index)

internal actual typealias NodeList = org.w3c.dom.NodeList

internal actual typealias Node = org.w3c.dom.Node

internal actual fun Node.clear(): Unit = clear()
internal actual inline fun Node.cloneNode_(deep: Boolean) = this.cloneNode(deep)