package com.nectar.doodle.system.impl

import com.nectar.doodle.dom.hasAutoOverflow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement


internal fun isNativeElement(element: HTMLElement) =
    element.hasAutoOverflow ||
//        isScrollBar  (node           ) ||
//        isScrollBar  (node.parentNode) ||
//    isType(node, "button") ||
//    isType(node, "label") ||
    element is HTMLButtonElement || element is HTMLInputElement

//private fun isType(element: Node?, type: String) = when {
//    element == null                        -> false
//    element.nodeName.toLowerCase() == type -> true
//    else                                   -> element.parentNode?.nodeName?.toLowerCase() == type
//}

//private fun isScrollPanel(element: Node) = element.hasAutoOverflow

//private fun isScrollBar(element: Node?): Boolean = when (element) {
//    is HTMLElement -> element.getAttribute("scrollbar")?.matches("true") ?: false
//    else           -> false
//}