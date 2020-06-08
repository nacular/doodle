package io.nacular.doodle

import org.w3c.dom.get
import kotlin.dom.clear

/**
 * Created by Nicholas Eddy on 8/9/19.
 */

actual inline operator fun NodeList.get(index: Int): Node? = get(index)

actual typealias NodeList = org.w3c.dom.NodeList

actual typealias Node = org.w3c.dom.Node

actual fun org.w3c.dom.Node.clear() = clear()