package com.nectar.doodle.system.impl

import com.nectar.doodle.HTMLButtonElement
import com.nectar.doodle.HTMLElement
import com.nectar.doodle.HTMLInputElement
import com.nectar.doodle.dom.hasAutoOverflow
import org.w3c.dom.events.EventTarget


internal fun isNativeElement(target: EventTarget?) =
    target is HTMLElement && (
        target.hasAutoOverflow ||
//        isScrollBar  (node           ) ||
//        isScrollBar  (node.parentNode) ||
//    isType(node, "button") ||
//    isType(node, "label") ||
        target is HTMLButtonElement || target is HTMLInputElement)

//private fun isType(element: Node?, type: String) = when {
//    element == null                        -> false
//    element.nodeName.toLowerCase() == type -> true
//    else                                   -> element.parentNode?.nodeName?.toLowerCase() == type
//}

internal fun nativeScrollPanel(target: EventTarget?) = target is HTMLElement && target.hasAutoOverflow

//private fun isScrollBar(element: Node?): Boolean = when (element) {
//    is HTMLElement -> element.getAttribute("scrollbar")?.matches("true") ?: false
//    else           -> false
//}