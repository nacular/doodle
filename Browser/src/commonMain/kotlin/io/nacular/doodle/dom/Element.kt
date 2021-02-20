package io.nacular.doodle.dom

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.Node
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.get


internal fun Node.childAt(index: Int): Node? = when {
    index >= 0 && index < childNodes.length -> childNodes[index]
    else                                    -> null
}

internal inline val Node.parent get() = parentNode

internal inline val Node.numChildren get() = childNodes.length

internal fun Node.index(of: Node) = (0 until childNodes.length).firstOrNull { childNodes[it] == of } ?: -1

internal inline fun Node.add(child: Node) = appendChild(child)

internal fun Node.addIfNotPresent(child: Node, at: Int) {
    if (child !== childNodes[at]) {
        insert(child, at)
    }
}

internal inline fun Node.insert(element: Node, index: Int) = insertBefore(element, childAt(index))

internal inline fun Node.remove(element: Node) = removeChild(element)

internal inline val HTMLElement.top    get() = offsetTop.toDouble   ()
internal inline val HTMLElement.left   get() = offsetLeft.toDouble  ()
internal inline val HTMLElement.width  get() = offsetWidth.toDouble ()
internal inline val HTMLElement.height get() = offsetHeight.toDouble()

internal inline val HTMLElement.hasAutoOverflow   get() = style.run { overflowX.isNotEmpty() || overflowY.isNotEmpty() }
internal inline val HTMLElement.hasScrollOverflow get() = style.run { overflowX == "scroll"  || overflowY == "scroll"  }

internal fun HTMLElement.scrollTo(point: Point) {
    try {
        scrollTo(point.x, point.y)
    } catch (ignored: Throwable) {
        // https://bugzilla.mozilla.org/show_bug.cgi?id=1671283
        scrollTop  = point.y
        scrollLeft = point.x
    }
}