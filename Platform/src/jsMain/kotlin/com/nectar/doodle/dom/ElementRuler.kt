package com.nectar.doodle.dom

import com.nectar.doodle.geometry.Size
import org.w3c.dom.HTMLElement

interface ElementRuler {
    fun size(element: HTMLElement): Size

    fun width (element: HTMLElement) = size(element).width
    fun height(element: HTMLElement) = size(element).height
}

class ElementRulerImpl(private val htmlFactory: HtmlFactory): ElementRuler {

    override fun width (element: HTMLElement) = measure(element) { width               }
    override fun height(element: HTMLElement) = measure(element) { height              }
    override fun size  (element: HTMLElement) = measure(element) { Size(width, height) }

    private fun <T> measure(element: HTMLElement, block: HTMLElement.() -> T): T {
        htmlFactory.root.insert(element, 0)

        return element.run(block).also {
            htmlFactory.root.removeChild(element)
        }
    }
}