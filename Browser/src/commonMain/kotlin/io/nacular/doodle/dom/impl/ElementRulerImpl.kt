package io.nacular.doodle.dom.impl

import io.nacular.doodle.HTMLElement
import io.nacular.doodle.dom.ElementRuler
import io.nacular.doodle.dom.HtmlFactory
import io.nacular.doodle.dom.height
import io.nacular.doodle.dom.insert
import io.nacular.doodle.dom.parent
import io.nacular.doodle.dom.width
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.willChange

internal class ElementRulerImpl(htmlFactory: HtmlFactory): ElementRuler {

    override fun width (element: HTMLElement) = measure(element) { width               }
    override fun height(element: HTMLElement) = measure(element) { height              }
    override fun size  (element: HTMLElement) = measure(element) { Size(width, height) }

    private val ruler = htmlFactory.root

    private fun <T> measure(element: HTMLElement, block: HTMLElement.() -> T): T {
        val old = element.style.willChange

        element.style.willChange = "transform"

        return when (element.parent) {
            null -> {
                ruler.insert(element, 0)

                element.run(block).also {
                    ruler.removeChild(element)
                }
            }
            else -> element.run(block)
        }.also {
            element.style.willChange = old
        }
    }
}