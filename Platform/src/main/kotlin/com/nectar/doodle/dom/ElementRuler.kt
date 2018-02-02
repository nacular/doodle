package com.nectar.doodle.dom

import com.nectar.doodle.geometry.Size
import org.w3c.dom.HTMLElement


interface ElementRuler {
    fun size(element: HTMLElement): Size

    fun width (element: HTMLElement) = size(element).width
    fun height(element: HTMLElement) = size(element).height
}

class ElementRulerImpl(private val htmlFactory: HtmlFactory): ElementRuler {
    override fun size(element: HTMLElement): Size {
        htmlFactory.body.insert(element, 0)

        val size = Size(element.offsetWidth.toDouble(), element.offsetHeight.toDouble())

        htmlFactory.body.removeChild(element)

        return size
    }
}