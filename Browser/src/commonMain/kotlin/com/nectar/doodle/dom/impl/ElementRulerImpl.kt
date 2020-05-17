package com.nectar.doodle.dom.impl

import com.nectar.doodle.HTMLElement
import com.nectar.doodle.dom.ElementRuler
import com.nectar.doodle.dom.HtmlFactory
import com.nectar.doodle.dom.height
import com.nectar.doodle.dom.insert
import com.nectar.doodle.dom.parent
import com.nectar.doodle.dom.width
import com.nectar.doodle.geometry.Size
import com.nectar.doodle.willChange

internal class ElementRulerImpl(htmlFactory: HtmlFactory): ElementRuler {

    override fun width (element: HTMLElement) = measure(element) { width               }
    override fun height(element: HTMLElement) = measure(element) { height              }
    override fun size  (element: HTMLElement) = measure(element) { Size(width, height) }

    private val ruler = htmlFactory.root

    private fun <T> measure(element: HTMLElement, block: HTMLElement.() -> T): T {
        val old = element.style.willChange

        element.style.willChange = "transform"

        return if (element.parent == null) {
            ruler.insert(element, 0)

            element.run(block).also {
                ruler.removeChild(element)
            }
        } else {
            element.run(block)
        }.also {
            element.style.willChange = old
        }
    }
}