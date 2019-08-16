package com.nectar.doodle.dom

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.geometry.Size

class ElementRulerImpl(private val htmlFactory: HtmlFactory): ElementRuler {

    override fun width (element: HTMLElement) = measure(element) { width               }
    override fun height(element: HTMLElement) = measure(element) { height              }
    override fun size  (element: HTMLElement) = measure(element) { Size(width, height) }

    private fun <T> measure(element: HTMLElement, block: HTMLElement.() -> T): T {
        val parent = element.parent
        val index  = parent?.index(element) ?: -1

        htmlFactory.root.insert(element, 0)

        return element.run(block).also {
            htmlFactory.root.removeChild(element)

            parent?.insert(element, index)
        }
    }
}