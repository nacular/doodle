package io.nacular.doodle.dom

internal actual fun Node.remove(element: Node): Node? = removeChild(element)