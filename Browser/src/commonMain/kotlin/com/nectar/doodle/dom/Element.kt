package com.nectar.doodle.dom

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.Node
import com.nectar.doodle.geometry.Point
import com.nectar.doodle.get


fun Node.childAt(index: Int): Node? = when {
    index >= 0 && index < childNodes.length -> childNodes[index]
    else                                    -> null
}

inline val Node.parent get() = parentNode

inline val Node.numChildren get() = childNodes.length

fun Node.index(of: Node) = (0 until childNodes.length).firstOrNull { childNodes[it] == of } ?: -1

inline fun Node.add(child: Node) = appendChild(child)

fun Node.addIfNotPresent(child: Node, at: Int) {
    if (child !== childNodes[at]) {
        insert(child, at)
    }
}

inline fun Node.insert(element: Node, index: Int) = insertBefore(element, childAt(index))

inline fun Node.remove(element: Node) = removeChild(element)

inline val HTMLElement.top    get() = offsetTop.toDouble   ()
inline val HTMLElement.left   get() = offsetLeft.toDouble  ()
inline val HTMLElement.width  get() = offsetWidth.toDouble ()
inline val HTMLElement.height get() = offsetHeight.toDouble()

inline val HTMLElement.hasAutoOverflow   get() = style.run { overflowX.isNotEmpty() || overflowY.isNotEmpty() }
inline val HTMLElement.hasScrollOverflow get() = style.run { overflowX == "scroll"  || overflowY == "scroll"  }

fun HTMLElement.clearVisualStyles() {
//    style.color      = ""
//    style.filter     = ""
//    style.border     = ""
//    style.background = ""
//
//    style.overflowX  = ""
//    style.overflowY  = ""
//    style.boxShadow  = ""
}

fun HTMLElement.clearBoundStyles() {
//    style.top          = ""
//    style.left         = ""
//    style.right        = ""
//    style.width        = ""
//    style.height       = ""
//    style.bottom       = ""
//    style.marginTop    = ""
//    style.marginLeft   = ""
//    style.marginRight  = ""
//    style.marginBottom = ""
}

fun HTMLElement.scrollTo(point: Point) {
    scrollTop  = point.y
    scrollLeft = point.x
}